package com.lkd.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.PageResult;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.lkd.http.controller.vo.OrderVO;
import com.lkd.http.controller.vo.Pager;
import com.lkd.service.ESOrderService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ESOrderServiceImpl implements ESOrderService {

    @Autowired
    private RestHighLevelClient client;

    // 订单查询
    @Override
    public Pager<OrderVO> search(Integer pageIndex, Integer pageSize, String orderNo, String openId, LocalDate startDate, LocalDate endDate) throws IOException {
        // 1.创建searchRequest
        SearchRequest request = new SearchRequest("order");
        // 2.准备DSL
        // 2-1 query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StrUtil.isNotEmpty(orderNo)) {
            boolQuery.must(QueryBuilders.termQuery("order_no", orderNo));
        }
        if (StrUtil.isNotEmpty(openId)) {
            boolQuery.must(QueryBuilders.termQuery("open_id", openId));
        }
        if (startDate != null && endDate != null) {
            boolQuery.must(QueryBuilders.rangeQuery("update_time").gt(startDate).lt(endDate));
        }
        request.source().query(boolQuery);
        // 2-2 分页
        int index = (pageIndex - 1) * pageSize;
        request.source().from(index).size(pageSize);
        // 2-3 排序
        request.source().sort("update_time", SortOrder.DESC);
        // 3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 4.解析响应
        return handleResponse(response);
    }

    // 解析响应
    private Pager<OrderVO> handleResponse(SearchResponse response) {
        // 4.解析响应
        SearchHits searchHits = response.getHits();
        // 4.1 获取总记录数
        long total = searchHits.getTotalHits().value;
        // 4.2 获取文档数组
        List<OrderVO> orderList = new ArrayList<>();
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            // 获取文档source
            String json = hit.getSourceAsString();
            OrderVO order = JSONUtil.toBean(json, OrderVO.class);
            orderList.add(order);
        }
        Pager<OrderVO> pager = new Pager<>();
        pager.setTotalCount(total);
        pager.setCurrentPageRecords(orderList);
        return pager;
    }
}

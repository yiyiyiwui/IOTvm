package com.lkd.filter;

import com.lkd.config.SkipAuthConfig;
import com.lkd.http.view.TokenObject;
import com.lkd.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT filter
 */
@Component
@Slf4j
public class JwtTokenFilter implements GlobalFilter, Ordered {

    @Autowired
    private SkipAuthConfig skipAuthConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        List<String> urls = skipAuthConfig.getUrls();
        log.info("skippath={},", urls);
//        获取请求路径
        String path = request.getURI().getPath();
        log.info("请求路径，path={}", path);

//        判断路径是否需要验证
        if (urls != null) {
            if (urls.stream().anyMatch(url -> path.startsWith(url))) {
                log.info("path={},不需要验证", path);
//            不需要验证的路径
                return chain.filter(exchange);
            }
        }
//        从请求头获取token
        String token = request.getHeaders().getFirst("Authorization");
        log.info("token={}", token);
        if (StringUtils.isBlank(token)) {
            log.error("token不存在！");
            return authError(response);
        }
        try {
//        解析token
            TokenObject tokenObject = JWTUtil.decode(token);
//        验证token
            JWTUtil.VerifyResult verifyResult = JWTUtil.verifyJwt(token, tokenObject);
            if (!verifyResult.isValidate()) {
                log.error("token验证不通过");
//            token验证不通过
                return authError(response);
            }
            //向headers中放用户id和登录类型
            ServerHttpRequest httpRequest = request.mutate()
                    .header("userId", tokenObject.getUserId().toString())
                    .header("loginType", tokenObject.getLoginType().toString())
                    .build();
            exchange.mutate().request(httpRequest);
        } catch (Exception e) {
            log.error("登录失败！");
            e.printStackTrace();
            return authError(response);
        }
        return chain.filter(exchange);

    }

    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * 认证错误输出
     *
     * @param resp 响应对象
     * @return
     */
    private Mono<Void> authError(ServerHttpResponse resp) {
        resp.setStatusCode(HttpStatus.UNAUTHORIZED);
        resp.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String returnStr = "token校验失败";
        DataBuffer buffer = resp.bufferFactory().wrap(returnStr.getBytes(StandardCharsets.UTF_8));
        return resp.writeWith(Flux.just(buffer));
    }
}
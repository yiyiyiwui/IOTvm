package com.lkd.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Strings;
import com.lkd.common.VMSystem;
import com.lkd.dao.UserDao;
import com.lkd.entity.UserEntity;
import com.lkd.exception.LogicException;
import com.lkd.feign.TaskServiceFeignClient;
import com.lkd.http.controller.vo.*;
import com.lkd.http.view.TokenObject;
import com.lkd.service.PartnerService;
import com.lkd.service.UserService;
import com.lkd.sms.SmsTemplate;
import com.lkd.utils.BCrypt;
import com.lkd.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Integer getOperatorCount() {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getRoleCode, "1002");

        return this.count(wrapper);
    }

    @Override
    public Integer getRepairerCount() {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getRoleCode, "1003");

        return this.count(wrapper);
    }

    @Override
    public Pager<UserEntity> findPage(long pageIndex, long pageSize, String userName, Integer roleId, Boolean isRepair) {
        Page<UserEntity> page = new Page(pageIndex, pageSize);

        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        if (!Strings.isNullOrEmpty(userName)) {
            wrapper.like(UserEntity::getUserName, userName);
        }
        if (roleId != null && roleId > 0) {
            wrapper.eq(UserEntity::getRoleId, roleId);
        }
        if (isRepair != null && isRepair == true) {
            wrapper.eq(UserEntity::getRoleCode, "1003");
        }
        if (isRepair != null && isRepair == false) {
            wrapper.eq(UserEntity::getRoleCode, "1002");
        }
        wrapper.ne(UserEntity::getRoleId, 1);
        this.page(page, wrapper);
        page.getRecords().forEach(u -> {
            u.setPassword("");
            u.setSecret("");
        });

        return Pager.build(page);
    }

    @Override
    public List<UserVO> getOperatorList(Long regionId) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(UserEntity::getRoleCode, "1002")
                .eq(UserEntity::getRegionId, regionId)
                .eq(UserEntity::getStatus, true);

        return this.list(wrapper)
                .stream()
                .map(u -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(u, vo);
                    vo.setRoleName(u.getRole().getRoleName());
                    vo.setRoleCode(u.getRoleCode());
                    vo.setUserId(u.getId());
                    return vo;
                }).collect(Collectors.toList());
    }

    @Override
    public List<UserVO> getRepairerList(Long regionId) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(UserEntity::getRoleCode, "1003")
                .eq(UserEntity::getRegionId, regionId)
                .eq(UserEntity::getStatus, true);

        return this.list(wrapper)
                .stream()
                .map(u -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(u, vo);
                    vo.setRoleName(u.getRole().getRoleName());
                    vo.setRoleCode(u.getRoleCode());
                    vo.setUserId(u.getId());
                    return vo;
                }).collect(Collectors.toList());
    }

    @Override
    public Integer getCountByRegion(Long regionId, Boolean isRepair) {
        var qw = new LambdaQueryWrapper<UserEntity>();
        qw.eq(UserEntity::getRegionId, regionId);
        if (isRepair) {
            qw.eq(UserEntity::getRoleId, 3);
        } else {
            qw.eq(UserEntity::getRoleId, 2);
        }

        return this.count(qw);
    }

    @Autowired
    private PartnerService partnerService;

    // 登录
    @Override
    public LoginResp login(LoginReq req) throws IOException {
        if (req.getLoginType() == VMSystem.LOGIN_ADMIN) {
            return this.adminLogin(req);//管理员登录
        } else if (req.getLoginType() == VMSystem.LOGIN_PARTNER) {
            return partnerService.login(req); // 合作商平台登录开发
        }else if(req.getLoginType()==VMSystem.LOGIN_EMP){
            return appLogin(req); // app端登录
        }
        LoginResp resp = new LoginResp();
        resp.setSuccess(false);
        resp.setMsg("不存在该账户");
        return resp;
    }

    // app登录
    private LoginResp appLogin(LoginReq req) throws IOException {
        // 1.比对验证码
        // 1-1 取出客户端提供
        String code = req.getCode();
        // 1-2 取出redis中
        String redisCode = redisTemplate.opsForValue().get(req.getMobile());
        // 1-3 比对不一致
        if (!StrUtil.equals(code,redisCode)) {
            throw new LogicException("验证码不正确");
        }
        // 2.比对手机号
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getMobile,req.getMobile());
        UserEntity userEntity = this.getOne(queryWrapper);
        if (userEntity==null) {
            throw new LogicException("此员工不存在");
        }
        // 3.登录成功
        //生成token，并存入返回对象
        LoginResp loginResp = okResp(userEntity, VMSystem.LOGIN_EMP);

        // 是否为运维（维修员）
        if (userEntity.getRoleId().equals(1003)) {
            loginResp.setRepair(true);
        }
        return loginResp;
    }

    @Autowired
    private TaskServiceFeignClient taskServiceFeignClient;

    @Override
    public Pager<UserWorkVO> searchUserWork(Long pageIndex, Long pageSize, String userName, Integer roleId, Boolean isRepair) {
        //查询用户分页
        var userPager = this.findPage(pageIndex, pageSize, userName, roleId, isRepair);
        //工作量列表
        var items = userPager.getCurrentPageRecords()
                .stream().map(u -> {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime start = LocalDateTime.of(now.getYear(), now.getMonth(), 1, 0, 0, 0);
                    var userWork = taskServiceFeignClient.getUserWork(u.getId(),
                            start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    userWork.setUserName(u.getUserName());
                    userWork.setRoleName(u.getRole().getRoleName());
                    userWork.setMobile(u.getMobile());
                    return userWork;
                }).collect(Collectors.toList());

        //封装分页对象
        Pager<UserWorkVO> result = Pager.buildEmpty();
        result.setPageIndex(userPager.getPageIndex());
        result.setPageSize(userPager.getPageSize());
        result.setTotalCount(userPager.getTotalCount());
        result.setTotalPage(userPager.getTotalPage());
        result.setCurrentPageRecords(items);
        return result;
    }


    @Autowired
    private SmsTemplate smsTemplate;

    // 发送短信验证码
    @Override
    public void sendSms(String mobile) {
        // 0.参数校验
        if (StrUtil.isBlank(mobile)) {
            throw new LogicException("参数非法");
        }
        // 1.根据手机号查询员工是否存在
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserEntity::getMobile,mobile);
        UserEntity userEntity = this.getOne(queryWrapper);
        if (userEntity==null) {
            throw new LogicException("此员工不存在");
        }
        // 2.生成5位验证码
        String code = RandomUtil.randomNumbers(5);
        code="12345"; // 为了方法测试
        // 3.阿里云发送短信
        // smsTemplate.sendSms(mobile,code);  测试期间注释
        // 4.向redis存取一份 5分钟
        redisTemplate.opsForValue().set(mobile,code, Duration.ofSeconds(300));

    }

    /**
     * 管理员登录
     *
     * @param req
     * @return
     * @throws IOException
     */
    private LoginResp adminLogin(LoginReq req) throws IOException {
        LoginResp resp = new LoginResp();
        String code = redisTemplate.boundValueOps(req.getClientToken()).get();
        if (Strings.isNullOrEmpty(code)) {
            resp.setMsg("验证码错误");
            return resp;
        }
        if (!req.getCode().equals(code)) {
            resp.setMsg("验证码错误");
            return resp;
        }
        QueryWrapper<UserEntity> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(UserEntity::getLoginName, req.getLoginName());
        UserEntity userEntity = this.getOne(qw);
        if (userEntity == null) {
            resp.setMsg("账户名或密码错误");
            return resp;
        }
        //验证密码
        boolean loginSuccess = BCrypt.checkpw(req.getPassword(), userEntity.getPassword());
        if (!loginSuccess) {
            resp.setMsg("账户名或密码错误");
            return resp;
        }
        return okResp(userEntity,VMSystem.LOGIN_ADMIN);
    }

    /**
     * 登录成功签发token
     *
     * @param userEntity
     * @param loginType
     * @return
     */
    private LoginResp okResp(UserEntity userEntity, Integer loginType) throws IOException {
        LoginResp resp = new LoginResp();
        resp.setSuccess(true);
        resp.setRoleCode(userEntity.getRoleCode());
        resp.setUserName(userEntity.getUserName());
        resp.setUserId(userEntity.getId());
        resp.setRegionId(userEntity.getRegionId().toString());
        resp.setMsg("登录成功");
        TokenObject tokenObject = new TokenObject();
        tokenObject.setUserId(userEntity.getId());
        tokenObject.setMobile(userEntity.getMobile());
        tokenObject.setLoginType(loginType);
        String token = JWTUtil.createJWTByObj(tokenObject);
        resp.setToken(token);
        return resp;
    }

}

package com.lkd.http.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.lkd.entity.UserEntity;
import com.lkd.feign.VmServiceFeignClient;
import com.lkd.http.controller.vo.*;
import com.lkd.service.RoleService;
import com.lkd.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private DefaultKaptcha kaptcha;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 发送短信验证码
    @GetMapping("/code/{mobile}")
    public void sendSms(@PathVariable("mobile") String mobile) {
        userService.sendSms(mobile);
    }

    /**
     * 获取图片验证码
     *
     * @param httpServletRequest
     * @param httpServletResponse
     */
    @GetMapping("/imageCode/{clientToken}")
    public void getImageCode(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable String clientToken) throws IOException {

        String createText = kaptcha.createText();
        BufferedImage challenge = kaptcha.createImage(createText);
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        ImageIO.write(challenge, "jpg", jpegOutputStream);
        byte[] captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
        httpServletResponse.setContentType("image/jpeg");
        ServletOutputStream responseOutputStream =
                httpServletResponse.getOutputStream();
        responseOutputStream.write(captchaChallengeAsJpeg);
        responseOutputStream.flush();
        responseOutputStream.close();
        //将验证码存入redis  2分钟超时
        redisTemplate.opsForValue().set(clientToken, createText, Duration.ofSeconds(120));
    }

    /**
     * 登录
     *
     * @param req
     * @return
     * @throws IOException
     */
    @PostMapping("/login")
    public LoginResp login(@RequestBody LoginReq req) throws IOException {
        return userService.login(req);
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return 实体
     */
    @GetMapping("/{id}")
    public UserVO findById(@PathVariable Integer id) {
        UserEntity userEntity = userService.getById(id);
        if (userEntity == null) {
            return null;
        }

        return convertToVM(userEntity);
    }

    /**
     * 新增
     *
     * @param req
     * @return 是否成功
     */
    @PostMapping
    public boolean add(@RequestBody UserReq req) {
        UserEntity user = new UserEntity();
        user.setUserName(req.getUserName());
        user.setRegionId(Long.valueOf(req.getRegionId()));
        user.setRegionName(req.getRegionName());
        user.setMobile(req.getMobile());
        user.setRoleId(req.getRoleId());
        user.setRoleCode(roleService.getById(req.getRoleId()).getRoleCode());
        user.setStatus(req.getStatus());
        user.setImage(req.getImage());
        String secret = System.currentTimeMillis() + "lkd";
        user.setSecret(secret);

        return userService.save(user);
    }

    /**
     * 修改
     *
     * @param id
     * @param req
     * @return 是否成功
     */
    @PutMapping("/{id}")
    public boolean update(@PathVariable Integer id, @RequestBody UserReq req) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUserName(req.getUserName());
        user.setRegionId(Long.valueOf(req.getRegionId()));
        user.setRegionName(req.getRegionName());
        user.setMobile(req.getMobile());
        user.setRoleId(req.getRoleId());
        user.setStatus(req.getStatus());

        return userService.updateById(user);
    }

    /**
     * 删除
     *
     * @param id
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Integer id) {
        return userService.removeById(id);
    }

    /**
     * 分页查询
     *
     * @param pageIndex 页码
     * @param pageSize  页大小
     * @param userName  用户名
     * @return 分页结果
     */
    @GetMapping("/search")
    public Pager<UserEntity> findPage(
            @RequestParam(value = "pageIndex", required = false, defaultValue = "1") long pageIndex,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") long pageSize,
            @RequestParam(value = "userName", required = false, defaultValue = "") String userName,
            @RequestParam(value = "roleId", required = false, defaultValue = "0") Integer roleId,
            @RequestParam(value = "isRepair", required = false, defaultValue = "") Boolean isRepair) {
        return userService.findPage(pageIndex, pageSize, userName, roleId, isRepair);
    }

    /**
     * 获取运营员数量
     *
     * @return
     */
    @GetMapping("/operaterCount")
    public Integer getOperatorCount() {
        return userService.getOperatorCount();
    }

    /**
     * 获取维修员数量
     *
     * @return
     */
    @GetMapping("/repairerCount")
    public Integer getRepairerCount() {
        return userService.getRepairerCount();
    }

    /**
     * 获取某区域下所有运营员
     *
     * @param regionId
     * @return
     */
    @GetMapping("/operators/{regionId}")
    public List<UserVO> getOperatorList(@PathVariable String regionId) {
        return userService.getOperatorList(Long.valueOf(regionId));
    }

    /**
     * 获取某区域下所有运维员
     *
     * @param regionId
     * @return
     */
    @GetMapping("/repairers/{regionId}")
    public List<UserVO> getRepairerList(@PathVariable String regionId) {
        return userService.getRepairerList(Long.valueOf(regionId));
    }

    private UserVO convertToVM(UserEntity userEntity) {
        UserVO userVO = new UserVO();
        userVO.setMobile(userEntity.getMobile());
        userVO.setLoginName(userEntity.getLoginName());
        userVO.setRoleId(userEntity.getRoleId());
        userVO.setRoleCode(userEntity.getRoleCode());
        userVO.setUserId(userEntity.getId());
        userVO.setRoleName(userEntity.getRole().getRoleName());
        userVO.setUserName(userEntity.getUserName());
        userVO.setStatus(userEntity.getStatus());
        userVO.setRegionId(userEntity.getRegionId());
        userVO.setRoleName(userEntity.getRole().getRoleName());
        userVO.setRegionName(userEntity.getRegionName());
        userVO.setImage(userEntity.getImage());

        return userVO;
    }

    /**
     * 搜索用户工作量列表
     *
     * @param pageIndex
     * @param pageSize
     * @param userName
     * @param roleId
     * @return
     **/
    @GetMapping("/searchUserWork")
    public Pager<UserWorkVO> searchUserWork(
            @RequestParam(value = "pageIndex", required = false, defaultValue = "1") long pageIndex,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") long pageSize,
            @RequestParam(value = "userName", required = false, defaultValue = "") String userName,
            @RequestParam(value = "roleId", required = false, defaultValue = "0") Integer roleId,
            @RequestParam(value = "isRepair", required = false, defaultValue = "") Boolean isRepair) {
        return userService.searchUserWork(pageIndex, pageSize, userName, roleId, isRepair);
    }

    @Autowired
    private VmServiceFeignClient vmServiceFeignClient;

    // 根据售货机获取维修人员列表
    @GetMapping("/repairerList/{innerCode}")
    public List<UserVO> repairerList(@PathVariable("innerCode") String innerCode) {
        // 根据设备号查询设备vo
        VmVO vmVO = vmServiceFeignClient.getInnerCode(innerCode);
        // 根据区域id查询运维人员列表
        return userService.getRepairerList(vmVO.getRegionId());
    }

    // 根据售货机获取运营人员列表
    @GetMapping("/operatorList/{innerCode}")
    public List<UserVO> operatorList(@PathVariable("innerCode") String innerCode) {
        // 根据设备号查询设备vo
        VmVO vmVO = vmServiceFeignClient.getInnerCode(innerCode);
        // 根据区域id查询运营人员列表
        return userService.getOperatorList(vmVO.getRegionId());
    }
}

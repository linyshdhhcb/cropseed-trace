package com.linyi.cropseed.trace.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.common.util.IdGenerator;
import com.linyi.cropseed.trace.common.util.SecurityUtils;
import com.linyi.cropseed.trace.dto.WxLoginDTO;
import com.linyi.cropseed.trace.entity.WxUser;
import com.linyi.cropseed.trace.mapper.WxUserMapper;
import com.linyi.cropseed.trace.service.WxUserService;
import com.linyi.cropseed.trace.vo.WxUserVO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

/**
 * 微信用户服务实现
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxUserServiceImpl extends ServiceImpl<WxUserMapper, WxUser> implements WxUserService {

    private final WxUserMapper wxUserMapper;

    private final RestTemplate restTemplate;

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxUserVO wxLogin(WxLoginDTO loginDTO) {
        // 调用微信API获取openid和session_key
        String code = loginDTO.getCode();
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code);

        try {
            String response = restTemplate.getForObject(url, String.class);
            // 解析响应获取openid和session_key
            return parseWxLoginResponse(response, loginDTO);
        } catch (Exception e) {
            log.error("微信登录失败", e);
            throw new RuntimeException("微信登录失败");
        }
    }

    @Override
    public WxUserVO getUserByOpenid(String openid) {
        WxUser user = wxUserMapper.selectByOpenid(openid);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxUserVO updateUserInfo(WxUserVO userVO) {
        Long userId = SecurityUtils.getCurrentUserId();
        WxUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 更新用户信息
        if (StrUtil.isNotBlank(userVO.getNickname())) {
            user.setNickname(userVO.getNickname());
        }
        if (StrUtil.isNotBlank(userVO.getAvatarUrl())) {
            user.setAvatarUrl(userVO.getAvatarUrl());
        }
        if (userVO.getGender() != null) {
            user.setGender(userVO.getGender());
        }
        if (StrUtil.isNotBlank(userVO.getCountry())) {
            user.setCountry(userVO.getCountry());
        }
        if (StrUtil.isNotBlank(userVO.getProvince())) {
            user.setProvince(userVO.getProvince());
        }
        if (StrUtil.isNotBlank(userVO.getCity())) {
            user.setCity(userVO.getCity());
        }

        updateById(user);
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxUserVO bindPhone(Long userId, String phone) {
        WxUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 检查手机号是否已被其他用户绑定
        WxUser existingUser = wxUserMapper.selectByPhone(phone);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST, "手机号已被其他用户绑定");
        }

        user.setPhone(phone);
        updateById(user);
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxUserVO unbindPhone(Long userId) {
        WxUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        user.setPhone(null);
        updateById(user);
        return convertToVO(user);
    }

    /**
     * 解析微信登录响应
     */
    private WxUserVO parseWxLoginResponse(String response, WxLoginDTO loginDTO) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String openid = jsonResponse.getStr("openid");
            String sessionKey = jsonResponse.getStr("session_key");

            if (StrUtil.isBlank(openid)) {
                throw new RuntimeException("微信登录失败：未获取到openid");
            }

            // 查询用户是否存在
            WxUser existingUser = wxUserMapper.selectByOpenid(openid);

            if (existingUser != null) {
                // 更新最后登录时间
                existingUser.setLastLoginTime(LocalDateTime.now());
                updateById(existingUser);
                return convertToVO(existingUser);
            } else {
                // 创建新用户
                WxUser newUser = new WxUser();
                newUser.setId(IdGenerator.generateId());
                newUser.setOpenid(openid);
                newUser.setStatus(1);
                newUser.setLastLoginTime(LocalDateTime.now());

                // 如果有用户信息，解析并设置
                if (StrUtil.isNotBlank(loginDTO.getUserInfo())) {
                    JSONObject userInfoJson = new JSONObject(loginDTO.getUserInfo());
                    String nickname = userInfoJson.getStr("nickName");
                    String avatarUrl = userInfoJson.getStr("avatarUrl");
                    Integer gender = userInfoJson.getInt("gender");

                    newUser.setNickname(nickname);
                    newUser.setAvatarUrl(avatarUrl);
                    newUser.setGender(gender);
                } else {
                    newUser.setNickname("微信用户");
                    newUser.setAvatarUrl("");
                    newUser.setGender(0);
                }

                save(newUser);
                return convertToVO(newUser);
            }
        } catch (Exception e) {
            log.error("解析微信登录响应失败", e);
            throw new RuntimeException("微信登录失败");
        }
    }

    /**
     * 转换为VO
     */
    private WxUserVO convertToVO(WxUser user) {
        WxUserVO vo = BeanUtil.copyProperties(user, WxUserVO.class);
        return vo;
    }
}

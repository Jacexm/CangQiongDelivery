package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.constant.WeChatLoginConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;

import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements  UserService{

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;


    public User userWechatLogin(UserLoginDTO userLoginDTO) {

        String userLoginCode = userLoginDTO.getCode();
        log.debug("小程序用户登录，code：{}",userLoginCode);


        if(userLoginCode == null || userLoginCode.isEmpty()){
            log.error("用户登录失败，code为空");
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        String wechatUserOpenId = getUserWeChatOpenId(userLoginCode);

        if(wechatUserOpenId == null || wechatUserOpenId.isEmpty()){
            log.error("用户登录失败，获取微信openid失败");
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        User user = userMapper.getUserByOpenId(wechatUserOpenId);

        if (user == null){
            log.info("新用户注册，微信openid：{}", wechatUserOpenId);
            //用户不存在，注册新用户
            User newUser = User.builder()
                    .openid(wechatUserOpenId)
                    .createTime(LocalDateTime.now())
                    .build();
            // TODO 可以完善新用户的其他信息，比如昵称、头像等
            userMapper.insert(newUser);

            return newUser;
        }
        return user;

    }

    /**
     * 根据code获取用户的微信openid
     * @param code
     * @return
     */
    private String getUserWeChatOpenId(String code){

        Map<String, String> wechatLioginParams = new HashMap<>();
        wechatLioginParams.put("appid", weChatProperties.getAppid());
        wechatLioginParams.put("secret", weChatProperties.getSecret());
        wechatLioginParams.put("js_code", code);
        wechatLioginParams.put("grant_type", WeChatLoginConstant.WECHAT_LOGIN_GRANT_TYPE);

        String wechatLoginResponse = HttpClientUtil.doGet(WeChatLoginConstant.WECHAT_LOGIN_URL,
                wechatLioginParams);

        JSONObject jsonObject = JSON.parseObject(wechatLoginResponse);
        String wechatOpenId = jsonObject.getString("openid");
        return wechatOpenId;
    }

}

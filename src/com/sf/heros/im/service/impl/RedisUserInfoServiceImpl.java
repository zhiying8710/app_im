package com.sf.heros.im.service.impl;

import java.util.Map;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.UserInfo;
import com.sf.heros.im.common.redis.RedisConnException;
import com.sf.heros.im.common.redis.RedisManagerV2;
import com.sf.heros.im.service.UserInfoService;

public class RedisUserInfoServiceImpl implements UserInfoService {

    private RedisManagerV2 rm;

    public RedisUserInfoServiceImpl() {
        this.rm = RedisManagerV2.getInstance();
    }

    @Override
    public UserInfo getById(String id) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        try {
            Map<String, String> hash = rm.hgetAll(Const.RedisKeyValConst.USER_INFO_KEY_PREFIX + id);
            if (hash != null && !hash.isEmpty()) {
                userInfo.setHead(hash.get(Const.RedisKeyValConst.USER_INFO_KEY_HEAD));
                userInfo.setNickName(hash.get(Const.RedisKeyValConst.USER_INFO_KEY_NICKNAME));
            }
            return userInfo;
        } catch (RedisConnException e) {
            return userInfo;
        }
    }

}

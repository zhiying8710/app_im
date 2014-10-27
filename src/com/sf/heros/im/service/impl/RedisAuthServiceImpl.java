package com.sf.heros.im.service.impl;

import java.util.Map;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.PropsLoader;
import com.sf.heros.im.common.bean.AuthCheck;
import com.sf.heros.im.common.exception.AuthChecException;
import com.sf.heros.im.common.redis.RedisConnException;
import com.sf.heros.im.common.redis.RedisManagerV2;
import com.sf.heros.im.service.AuthService;

public class RedisAuthServiceImpl implements AuthService {

    private static final Logger logger = Logger.getLogger(RedisAuthServiceImpl.class);

    private RedisManagerV2 rm;

    public RedisAuthServiceImpl() {
        this.rm = RedisManagerV2.getInstance();
    }

    @Override
    public AuthCheck check(String userId, String token) {
        AuthCheck check = new AuthCheck();
        try {
            Map<String, String> info = rm.hgetAll(Const.RedisConst.USER_STATUS_KEY_PRIFIX + userId);

            if (PropsLoader.get(Const.PropsConst.AUTH_CHECK_SO_ILLAG, false)) {
                if (info.isEmpty() || Const.RedisConst.USER_STATUS_VAL_ONLINE_ONLINE.equals(info.get(Const.RedisConst.USER_SATATUS_KEY_ONLINE))) {
                    check.setIllegal(false);
                    check.setPass(false);
                    check.setOnline(false);
                    return check;
                }
            }
            check.setIllegal(true);
            String soOnline = info.get(Const.RedisConst.USER_STATUS_KEY_SO_ONLINE);
            if (info.isEmpty() ||
                    Const.RedisConst.USER_STATUS_VAL_SO_ONLINE_OFFLINE.equals(soOnline)) {
                check.setOnline(false);
                check.setPass(true);
                return check;
            }
            String oToken = info.get(Const.RedisConst.USER_STATUS_KEY_TOKEN);
            check.setOnline(true);
            if (!oToken.equals(token)) {
                check.setPass(false);
                return check;
            }
            check.setPass(true);
            return check;
        } catch (RedisConnException e) {
            logger.error("check user auth error", e);
            throw new AuthChecException("get auth check info from redis error.");
        }
    }

}

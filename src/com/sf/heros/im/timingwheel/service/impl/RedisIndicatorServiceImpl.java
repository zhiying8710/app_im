package com.sf.heros.im.timingwheel.service.impl;

import com.sf.heros.im.common.redis.RedisConnException;
import com.sf.heros.im.common.redis.RedisManagerV2;
import com.sf.heros.im.timingwheel.Slot;
import com.sf.heros.im.timingwheel.service.IndicatorService;

public class RedisIndicatorServiceImpl implements IndicatorService {

    private static final String INDICATOR_KEY = "__indicator";

    private RedisManagerV2 rm;

    public RedisIndicatorServiceImpl() {
        this.rm = RedisManagerV2.getInstance();
    }

    @Override
    public Integer get(String e) {
        String slotIdx = null;
        try {
            slotIdx = rm.hget(true, INDICATOR_KEY, e);
        } catch (RedisConnException e1) {
        }
        if (slotIdx == null) {
            return null;
        }
        return Integer.valueOf(slotIdx);
    }

    @Override
    public void remove(String e) {
        rm.hdel(true, INDICATOR_KEY, e);
    }

    @Override
    public void put(String e, Slot slot) {
        rm.hset(true, INDICATOR_KEY, e, slot.getId() + "");
    }

}

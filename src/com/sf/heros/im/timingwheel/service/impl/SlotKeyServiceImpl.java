package com.sf.heros.im.timingwheel.service.impl;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.PropsLoader;
import com.sf.heros.im.common.redis.RedisManagerV2;
import com.sf.heros.im.timingwheel.service.SlotKeyService;

public class SlotKeyServiceImpl implements SlotKeyService {

    private static final String SLOT_KEY_PREFIX = "__slot_";
    private static final String SERVER_ID = PropsLoader.get(Const.PropsConst.SERVER_ID);
    private RedisManagerV2 rm = RedisManagerV2.getInstance();

    @Override
    public String geneKey(int id) {
        return SLOT_KEY_PREFIX + SERVER_ID + Const.CommonConst.KEY_SEP + id;
    }

	@Override
	public void clear(int id) {
		rm.del(geneKey(id));
	}

}

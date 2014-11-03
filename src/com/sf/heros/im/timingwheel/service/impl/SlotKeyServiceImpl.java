package com.sf.heros.im.timingwheel.service.impl;

import java.util.UUID;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.ImUtils;
import com.sf.heros.im.common.PropsLoader;
import com.sf.heros.im.timingwheel.service.SlotKeyService;

public class SlotKeyServiceImpl implements SlotKeyService {

    private static final String SLOT_KEY_PREFIX = "__slot_";
    private static final String SERVER_NAME = PropsLoader.get(Const.PropsConst.SERVER_NAME, ImUtils.getUniqueId(UUID.randomUUID().toString()));

    @Override
    public String geneKey(int id) {
        return SLOT_KEY_PREFIX + SERVER_NAME + Const.CommonConst.KEY_SEP + id;
    }

}

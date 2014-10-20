package com.sf.heros.im.timingwheel.service.impl;

import com.sf.heros.im.timingwheel.service.SlotKeyService;

public class SlotKeyServiceImpl implements SlotKeyService {

    private static final String SLOT_KEY_PREFIX = "__slot_";

    @Override
    public String geneKey(int id) {
        return SLOT_KEY_PREFIX + id;
    }

}

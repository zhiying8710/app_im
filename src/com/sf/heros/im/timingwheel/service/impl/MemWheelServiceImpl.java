package com.sf.heros.im.timingwheel.service.impl;

import java.util.LinkedHashMap;

import com.sf.heros.im.timingwheel.Slot;
import com.sf.heros.im.timingwheel.service.WheelService;

public class MemWheelServiceImpl implements WheelService {

    private LinkedHashMap<Integer, Slot> wheel = new LinkedHashMap<Integer, Slot>();


    @Override
    public void add(Slot slot) {

        wheel.put(slot.getId(), slot);

    }

    @Override
    public Slot get(Integer idx) {
        if (idx == null || idx < 0) {
            return null;
        }
        return wheel.get(idx);
    }

    @Override
    public int size() {
        return wheel.size();
    }

}

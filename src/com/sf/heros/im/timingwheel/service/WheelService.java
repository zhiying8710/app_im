package com.sf.heros.im.timingwheel.service;

import com.sf.heros.im.timingwheel.Slot;

public interface WheelService {

    public void add(Slot slot);

    public Slot get(Integer idx);

    public int size();

}

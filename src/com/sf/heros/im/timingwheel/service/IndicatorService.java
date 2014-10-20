package com.sf.heros.im.timingwheel.service;

import com.sf.heros.im.timingwheel.Slot;

public interface IndicatorService {

    Integer get(String e);

    void remove(String e);

    void put(String e, Slot slot);

}

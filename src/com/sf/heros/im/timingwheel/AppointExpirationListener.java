package com.sf.heros.im.timingwheel;

public abstract class AppointExpirationListener {

    /**
     * Invoking when a expired event occurs.
     *
     * @param expiredObject
     */
    public abstract void expired(String expiredObject);

}

package com.sf.heros.im.thrift;

import org.apache.thrift.TException;

import com.sf.heros.im.common.Counter;

public class ImCounterIfaceImpl implements ImCounter.Iface {

    @Override
    public long get_onlines() throws TException {
        return Counter.OnlinesCounter.INSTANCE.get();
    }

    @Override
    public long get_channels() throws TException {
        return Counter.ConnsCounter.INSTANCE.get();
    }

}

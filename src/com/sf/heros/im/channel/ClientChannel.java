package com.sf.heros.im.channel;

import io.netty.channel.Channel;

public interface ClientChannel extends Channel {

    public Long getId();

    public boolean group();

    public boolean ungroup();
}

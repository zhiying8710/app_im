package com.sf.heros.im.resp.mq;

import java.io.IOException;

import com.sf.heros.im.common.bean.msg.Resp;

public class RespProducer extends RespEndPoint {

    public RespProducer() {
        super();
    }

    public void sendMessage(Resp resp, String serverId) throws IOException {
        channel.basicPublish("", getEndPointNameByServerId(serverId), null, resp.toJson().getBytes());
    }

}

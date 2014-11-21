package com.sf.heros.im.resp.mq;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.PropsLoader;

public abstract class RespEndPoint {

    private static final String SERVER_ID = PropsLoader.get(Const.PropsConst.SERVER_ID);

    protected Channel channel;
    protected Connection conn;
    protected String endPointName;

    public RespEndPoint() {
        this.endPointName = getEndPointNameByServerId(SERVER_ID);
        ConnectionFactory fac = new ConnectionFactory();

        fac.setHost(PropsLoader.get(Const.PropsConst.MQ_HOST, "127.0.0.1"));
        fac.setPort(PropsLoader.get(Const.PropsConst.MQ_PORT, 5672));
        try {
            this.conn = fac.newConnection();
            this.channel = conn.createChannel();

            this.channel.queueDeclare(endPointName, false, false, false, null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }

    }

    public void close() throws IOException {
        this.channel.close();
        this.conn.close();
    }

    public String getEndPointNameByServerId(String serverId) {
        return "__im_resp_queue_for_" + serverId;
    }

}

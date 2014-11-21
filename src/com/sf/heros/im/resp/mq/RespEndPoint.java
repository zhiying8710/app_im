package com.sf.heros.im.resp.mq;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sf.heros.im.AppMain;
import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.PropsLoader;

public abstract class RespEndPoint {

	private static final Logger logger = Logger.getLogger(RespEndPoint.class);

    protected Channel channel;
    protected Connection conn;
    protected String endPointName;

    public RespEndPoint() {
        this.endPointName = getEndPointNameByServerId(AppMain.SERVER_UNIQUE_ID);
        ConnectionFactory fac = new ConnectionFactory();

        fac.setHost(PropsLoader.get(Const.PropsConst.MQ_HOST, "127.0.0.1"));
        fac.setPort(PropsLoader.get(Const.PropsConst.MQ_PORT, 5672));
        fac.setUsername(PropsLoader.get(Const.PropsConst.MQ_USERNAME, "guest"));
        fac.setPassword(PropsLoader.get(Const.PropsConst.MQ_PASSWORD, "guest"));
        try {
            this.conn = fac.newConnection();
            this.channel = conn.createChannel();

            this.channel.queueDeclare(endPointName, false, false, false, null);
        } catch (Exception e) {
        	logger.error("conn to mq err.", e);
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

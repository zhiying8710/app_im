package com.sf.heros.im.channel.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetAddress;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import com.sf.heros.im.channel.ClientChannel;
import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.PropsLoader;
import com.twitter.service.snowflake.IdWorker;
import com.twitter.service.snowflake.Snowflake;

public class ClientChannelIdUtil {

    private static final Logger logger = Logger.getLogger(ClientChannelIdUtil.class);

    public static Long getId(ChannelHandlerContext ctx) {
        return getId(ctx.channel());
    }

    public static Long getId(Channel channel) {
        if (!(channel instanceof ClientChannel)) {
            return Const.ProtocolConst.EMPTY_SESSION_ID;
        }
        return ((ClientChannel)channel).getId();
    }

    private static Lock clientLock = new ReentrantReadWriteLock().writeLock();
    private static String snowflakeUsrAgent = PropsLoader.get(Const.PropsConst.CHANNEL_ID_THRIFT_USRAGENT, "im");
    private static String clientHost = PropsLoader.get(Const.PropsConst.CHANNEL_ID_THRIFT_HOST, "");
    private static int clientPort = PropsLoader.get(Const.PropsConst.CHANNEL_ID_THRIFT_PORT, -1);
    private static boolean useLocalIdWorker = false;
    private static IdWorker localIdWorker;

    public static Long getId() {
        clientLock.lock();
        try {
            if (useLocalIdWorker) {
                return getFromLocal();
            } else {
                return getFromRemote();
            }
        } finally {
            clientLock.unlock();
        }

    }

    private static Long getFromLocal() {
        try {
            return localIdWorker.get_id(snowflakeUsrAgent);
        } catch (Exception e) {
            return Const.ProtocolConst.EMPTY_SESSION_ID;
        }
    }

    private static Long getFromRemote() {
        TFramedTransport transport = new TFramedTransport(new TSocket(clientHost, clientPort));
        try {
            transport.open();
        } catch (TTransportException e) {
            logger.error("open Snowflake transport error.", e);
            if (transport.isOpen()) {
                transport.close();
            }
            return Const.ProtocolConst.EMPTY_SESSION_ID;
        }
        TProtocol protocol = new  TBinaryProtocol(transport);
        Snowflake.Client client = new Snowflake.Client(protocol);
        try {
            return client.get_id(snowflakeUsrAgent);
        } catch (Exception e) {
            logger.error("get a id for a new channel error.", e);
            return Const.ProtocolConst.EMPTY_SESSION_ID;
        } finally {
            transport.close();
        }
    }

    public static void useLocalIdWorker() {
        useLocalIdWorker = true;
        try {
            int workerId = PropsLoader.get(Const.PropsConst.SERVER_NAME, InetAddress.getLocalHost().getHostName()).hashCode() % 31;
            localIdWorker = new IdWorker(workerId, workerId);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("init local idworker failed.");
        }
    }

}

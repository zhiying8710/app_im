package com.sf.heros.im.test.handler;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.reflect.TypeToken;
import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.ImUtils;
import com.sf.heros.im.common.bean.msg.ReqMsg;
import com.sf.heros.im.common.bean.msg.RespMsg;
import com.sf.heros.im.test.service.ReqMsgService;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class LogicMsgHandler extends ChannelInboundHandlerAdapter {

    private ReqMsgService reqMsgService;
    private String userId;
    private String token;
    private String toUserId;

    public LogicMsgHandler(ReqMsgService reqMsgService, String userId, String token, String toUserId) {
        super();
        this.reqMsgService = reqMsgService;
        this.userId = userId;
        this.token = token;
        this.toUserId = toUserId;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server close me." + this.userId + ":::" + this.token);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ReqMsg reqMsg = new ReqMsg();
        reqMsg.setType(Const.ReqMsgConst.TYPE_STRING_MSG);
        reqMsg.setToData(Const.ReqMsgConst.DATA_FROM_USERID, userId);
        reqMsg.setToData(Const.ReqMsgConst.DATA_TO_USERID, toUserId);
        new Timer().scheduleAtFixedRate(new SimpleTimerTask(ctx.channel(), reqMsg, userId), new Random().nextInt(3000), new Random().nextInt(3000));

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        if (!(msg instanceof RespMsg)) {
            super.channelRead(ctx, msg);
            return;
        }

        RespMsg respMsg = (RespMsg) msg;
        int type = respMsg.getType();
        switch (type) {
        case Const.RespMsgConst.TYPE_ASK_LOGIN:
            ReqMsg reqMsg = new ReqMsg();
            reqMsg.setToData(Const.ReqMsgConst.DATA_AUTH_USERID, userId);
            reqMsg.setToData(Const.ReqMsgConst.DATA_AUTH_TOKEN, token);
            reqMsg.setType(Const.ReqMsgConst.TYPE_LOGIN);
            ctx.channel().writeAndFlush(ImUtils.getBuf(ctx.alloc(), reqMsg));
            break;
        case Const.RespMsgConst.TYPE_LOGIN:
            SessionIdHolder.add(userId, respMsg.getFromData(Const.RespMsgConst.DATA_KEY_LOGIN_SESSIONID).toString());

            reqMsg = new ReqMsg();
            reqMsg.setType(Const.ReqMsgConst.TYPE_STRING_MSG);
            reqMsg.setToData(Const.ReqMsgConst.DATA_TO_USERID, toUserId);
            reqMsg.setSid(SessionIdHolder.get(userId));
            new Timer().scheduleAtFixedRate(new SimpleTimerTask(ctx.channel(), reqMsg, userId), new Random().nextInt(3000), new Random().nextInt(3000));

            System.err.println(SessionIdHolder.all());

            break;
        case Const.RespMsgConst.TYPE_KICKED:
            if (!ctx.isRemoved() && ctx.channel() != null && ctx.channel().isRegistered()) {
                ctx.channel().close();
            }
            break;
        case Const.RespMsgConst.TYPE_ACK:
            System.err.println("got ack for " + respMsg);
            String me = respMsg.getFromData(Const.RespAckMsgConst.DATA_SRC_FROM_USERID, "null").toString();
            String to = null;
            try {
                to = respMsg.getFromData(Const.RespAckMsgConst.DATA_SRC_TO_USERID, "null").toString();
            } catch (Exception e) {
                to = "null";
            }
            String time = respMsg.getFromData(Const.RespAckMsgConst.DATA_SRC_TIME, "0").toString();
            String srcType = respMsg.getFromData(Const.ReqAckMsgConst.DATA_SRC_TYPE, "-1").toString();
//            reqMsgService.delUnAck(msgNo);
            System.out.println(me + "_" + to + "_" + time + "_" + srcType + " is acked.");
            break;
        case Const.RespMsgConst.TYPE_MSG_HANDLER_ERROR:
            me = respMsg.getFromData(Const.RespAckMsgConst.DATA_SRC_FROM_USERID, "null").toString();
            try {
                to = respMsg.getFromData(Const.RespAckMsgConst.DATA_SRC_TO_USERID, "null").toString();
            } catch (Exception e) {
                to = "null";
            }
            time = respMsg.getFromData(Const.RespAckMsgConst.DATA_SRC_TIME).toString();
            String remark = respMsg.getData().get(Const.RespAckMsgConst.DATA_KEY_REMARK).toString();
            srcType = respMsg.getFromData(Const.ReqAckMsgConst.DATA_SRC_TYPE).toString();
            System.err.println("server handle msg : " + me + "_" + to + "_" + time + "_" + srcType + " error, remark : " + remark);
//            System.err.println("server handle msg : " + reqMsgService.get(msgNo) + " error, remark : " + remark);
            break;
        case Const.RespMsgConst.TYPE_REQ_PING:
            reqMsg = new ReqMsg();
            reqMsg.setType(Const.ReqMsgConst.TYPE_PING);
            ctx.channel().writeAndFlush(ImUtils.getBuf(ctx.alloc(), reqMsg));
            break;
        case Const.RespMsgConst.TYPE_SERVER_ERR:
            System.err.println("server err.");
            ctx.channel().close();
            break;
        case Const.RespMsgConst.TYPE_OFFLINE_MSG:
            List<String> offlineMsgs = Const.CommonConst.GSON.fromJson(respMsg.getData().get(Const.RespMsgConst.DATA_KEY_OFFLINE_MSGS).toString(), new TypeToken<List<String>>(){}.getType());
            for (String offlineMsg : offlineMsgs) {
                System.err.println("got offline msg : " + offlineMsg);
            }
            break;
        case Const.RespMsgConst.TYPE_STRING_MSG:
        case Const.RespMsgConst.TYPE_VOICE_MSG:
            System.err.println("got " + type + " msg " + respMsg);
            ReqMsg ackReqMsg = new ReqMsg();
            ackReqMsg.setType(Const.ReqMsgConst.TYPE_ACK);
            ackReqMsg.setToData(Const.ReqAckMsgConst.DATA_SRC_TO_USERID, userId);
            ackReqMsg.setToData(Const.ReqAckMsgConst.DATA_SRC_FROM_TIME, respMsg.getTime());
            ackReqMsg.setToData(Const.ReqAckMsgConst.DATA_SRC_FROM_USERID, respMsg.getFromData(Const.RespMsgConst.DATA_KEY_FROM_USER_ID));
            ctx.channel().writeAndFlush(ImUtils.getBuf(ctx.alloc(), ackReqMsg));
            break;
        default:
            break;
        }

    }

}

class SimpleTimerTask extends TimerTask {

    private Channel channel;
    private ReqMsg reqMsg;
    private AtomicInteger no = new AtomicInteger(1);
    private String userId;

    public SimpleTimerTask(Channel channel, ReqMsg reqMsg, String userId) {
        super();
        this.channel = channel;
        this.reqMsg = reqMsg;
        this.userId = userId;
    }

    @Override
    public void run() {
        if (channel == null || !channel.isActive() || no.get() >= 1) {
            this.cancel();
            Thread.interrupted();
        }
        reqMsg.setToData(Const.ReqMsgConst.DATA_CONTENT, "I'm " + this.userId + ", and now time is " + new Date().getTime());
        try {
            channel.writeAndFlush(ImUtils.getBuf(channel.alloc(), reqMsg));
        } catch (UnsupportedEncodingException e) {
        }
    }


}

class SessionIdHolder {

    private static final Map<String, String> holder = new ConcurrentHashMap<String, String>();

    public static void add(String userId, String sessionId) {
        holder.put(userId, sessionId);
    }

    public static Map<String, String> all() {
        return holder;
    }

    public static String get(String userId) {

        return holder.get(userId);

    }


}

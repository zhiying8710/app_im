package com.sf.heros.im.test.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

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
import com.sf.heros.im.common.bean.msg.Req;
import com.sf.heros.im.common.bean.msg.Resp;
import com.sf.heros.im.test.service.ReqMsgService;

public class LogicMsgHandler extends ChannelInboundHandlerAdapter {

    private String userId;
    private String token;
    private String toUserId;

    public LogicMsgHandler(ReqMsgService reqMsgService, String userId, String token, String toUserId) {
        super();
        this.userId = userId;
        this.token = token;
        this.toUserId = toUserId;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server close me." + this.userId + ":::" + this.token);
    }

    public void writeAndFlush(Channel channel, Req req) throws Exception {
        Long sid = req.getSid();
        int type = req.getType();
        Map<String, Object> data = req.getData();
        ByteBuf buf = channel.alloc().buffer().writeLong(sid).writeInt(type);
        if (data != null && !data.isEmpty()) {
            byte[] dbs = Const.CommonConst.GSON.toJson(data).getBytes("utf-8");
            buf.writeInt(dbs.length);
            buf.writeBytes(dbs);
        } else {
            buf.writeInt(0);
        }
        channel.writeAndFlush(buf);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        Req reqMsg = new Req(Const.ProtocolConst.EMPTY_SESSION_ID, Const.ReqConst.TYPE_STRING_MSG, null);
////        reqMsg.setType(Const.ReqConst.TYPE_STRING_MSG);
//        reqMsg.setToData(Const.ReqConst.DATA_FROM_USERID, userId);
//        reqMsg.setToData(Const.ReqConst.DATA_TO_USERID, toUserId);
//        new Timer().scheduleAtFixedRate(new SimpleTimerTask(this, ctx.channel(), reqMsg, userId), new Random().nextInt(3000), new Random().nextInt(3000));
    	Req reqMsg = new Req(Const.ProtocolConst.EMPTY_SESSION_ID, Const.ReqConst.TYPE_LOGIN, null);
        reqMsg.setToData(Const.ReqConst.DATA_AUTH_USERID, userId);
        reqMsg.setToData(Const.ReqConst.DATA_AUTH_TOKEN, token);
        reqMsg.setType(Const.ReqConst.TYPE_LOGIN);
        writeAndFlush(ctx.channel(), reqMsg);

        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        if (!(msg instanceof Resp)) {
            super.channelRead(ctx, msg);
            return;
        }

        Resp respMsg = (Resp) msg;
        int type = respMsg.getType();
        switch (type) {
        case Const.RespConst.TYPE_ASK_LOGIN:
            Req reqMsg = new Req(Const.ProtocolConst.EMPTY_SESSION_ID, Const.ReqConst.TYPE_LOGIN, null);
            reqMsg.setToData(Const.ReqConst.DATA_AUTH_USERID, userId);
            reqMsg.setToData(Const.ReqConst.DATA_AUTH_TOKEN, token);
            reqMsg.setType(Const.ReqConst.TYPE_LOGIN);
            writeAndFlush(ctx.channel(), reqMsg);
            break;
        case Const.RespConst.TYPE_LOGIN:
            SessionIdHolder.add(userId, respMsg.getSid());

            reqMsg = new Req(SessionIdHolder.get(userId), Const.ReqConst.TYPE_STRING_MSG, null);
            reqMsg.setToData(Const.ReqConst.DATA_TO_USERID, toUserId);
            reqMsg.setToData(Const.ReqConst.DATA_MSG_NO, ImUtils.getUniqueId("1"));
            new Timer().scheduleAtFixedRate(new SimpleTimerTask(this, ctx.channel(), reqMsg, userId), new Random().nextInt(3000), new Random().nextInt(3000));

            System.err.println(SessionIdHolder.all());

            break;
        case Const.RespConst.TYPE_KICKED:
            if (!ctx.isRemoved() && ctx.channel() != null && ctx.channel().isRegistered()) {
                ctx.channel().close();
            }
            break;
        case Const.RespConst.TYPE_ACK:
            System.err.println("got ack for " + respMsg);
            String me = respMsg.getFromData(Const.RespAckConst.DATA_SRC_FROM_USERID, "null").toString();
            String to = null;
            try {
                to = respMsg.getFromData(Const.RespAckConst.DATA_SRC_TO_USERID, "null").toString();
            } catch (Exception e) {
                to = "null";
            }
            String time = respMsg.getFromData(Const.RespAckConst.DATA_SRC_MSG_NO, "0").toString();
//            reqMsgService.delUnAck(msgNo);
            System.out.println(me + "_" + to + "_" + time + " is acked.");
            break;
        case Const.RespConst.TYPE_MSG_HANDLER_ERROR:
            me = respMsg.getFromData(Const.RespAckConst.DATA_SRC_FROM_USERID, "null").toString();
            try {
                to = respMsg.getFromData(Const.RespAckConst.DATA_SRC_TO_USERID, "null").toString();
            } catch (Exception e) {
                to = "null";
            }
            time = respMsg.getFromData(Const.RespAckConst.DATA_SRC_MSG_NO).toString();
            String remark = respMsg.getFromData(Const.RespAckConst.DATA_KEY_REMARK).toString();
            System.err.println("server handle msg : " + me + "_" + to + "_" + time + " error, remark : " + remark);
//            System.err.println("server handle msg : " + reqMsgService.get(msgNo) + " error, remark : " + remark);
            break;
        case Const.RespConst.TYPE_REQ_PING:
            reqMsg = new Req(SessionIdHolder.get(userId), Const.ReqConst.TYPE_PING, null);
            reqMsg.setType(Const.ReqConst.TYPE_PING);
            writeAndFlush(ctx.channel(), reqMsg);
            break;
        case Const.RespConst.TYPE_SERVER_ERR:
            System.err.println("server err.");
            ctx.channel().close();
            break;
        case Const.RespConst.TYPE_OFFLINE_MSG:
            List<String> offlineMsgs = Const.CommonConst.GSON.fromJson(respMsg.getFromData(Const.RespConst.DATA_KEY_OFFLINE_MSGS).toString(), new TypeToken<List<String>>(){}.getType());
            for (String offlineMsg : offlineMsgs) {
                System.err.println("got offline msg : " + offlineMsg);
            }
            break;
        case Const.RespConst.TYPE_STRING_MSG:
        case Const.RespConst.TYPE_VOICE_MSG:
            System.err.println("got " + type + " msg " + respMsg);
            Req ackReqMsg = new Req(SessionIdHolder.get(userId), Const.ReqConst.TYPE_ACK, null);
            ackReqMsg.setType(Const.ReqConst.TYPE_ACK);
            ackReqMsg.setToData(Const.ReqAckConst.DATA_SRC_TO_USERID, userId);
            ackReqMsg.setToData(Const.ReqAckConst.DATA_SRC_MSG_NO, respMsg.getMsgNo());
            ackReqMsg.setToData(Const.ReqAckConst.DATA_SRC_FROM_USERID, respMsg.getFromData(Const.RespConst.DATA_KEY_FROM_USER_ID));
            writeAndFlush(ctx.channel(), ackReqMsg);
            break;
        default:
            break;
        }

    }

}

class SimpleTimerTask extends TimerTask {

    private Channel channel;
    private Req reqMsg;
    private AtomicInteger no = new AtomicInteger(1);
    private String userId;
    private LogicMsgHandler handler;

    public SimpleTimerTask(LogicMsgHandler handler, Channel channel, Req reqMsg, String userId) {
        super();
        this.channel = channel;
        this.reqMsg = reqMsg;
        this.userId = userId;
        this.handler = handler;
    }

    @Override
    public void run() {
        if (channel == null || !channel.isActive() || no.get() >= 1) {
            this.cancel();
            Thread.interrupted();
        }
        reqMsg.setToData(Const.ReqConst.DATA_CONTENT, "I'm " + this.userId + ", and now time is " + new Date().getTime());
//        reqMsg.setToData(Const.ReqConst.DATA_CONTENT, "");
        try {
            handler.writeAndFlush(channel, reqMsg);
        } catch (Exception e) {
        }
    }


}

class SessionIdHolder {

    private static final Map<String, Long> holder = new ConcurrentHashMap<String, Long>();

    public static void add(String userId, Long sessionId) {
        holder.put(userId, sessionId);
    }

    public static Map<String, Long> all() {
        return holder;
    }

    public static Long get(String userId) {

        return holder.get(userId);

    }


}

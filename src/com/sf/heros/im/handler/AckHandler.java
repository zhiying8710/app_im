package com.sf.heros.im.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.msg.AckRespMsg;
import com.sf.heros.im.common.bean.msg.ReqMsg;
import com.sf.heros.im.common.bean.msg.RespMsg;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UnAckRespMsgService;

@Sharable
public class AckHandler extends CommonInboundHandler {

    private static final Logger logger = Logger.getLogger(AckHandler.class);

    private RespMsgService respMsgService;
    private UnAckRespMsgService unAckRespMsgService;
    private SessionService sessionService;

    public AckHandler(RespMsgService respMsgService,
            UnAckRespMsgService unAckRespMsgService, SessionService sessionService) {
        super();
        this.respMsgService = respMsgService;
        this.unAckRespMsgService = unAckRespMsgService;
        this.sessionService = sessionService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (!(msg instanceof ReqMsg)) {
            super.channelRead(ctx, msg);
            return;
        }
        ReqMsg reqMsg = (ReqMsg) msg;
        String sessionId = reqMsg.getSid();
        try {
            int reqType = reqMsg.getType();
            switch (reqType) {
            case Const.ReqMsgConst.TYPE_ACK: // only {@link Const.ReqMsgConst.TYPE_STRING_MSG/TYPE_VOICE_MSG} need ack.
                String unAckMsgId = getUnAckMsgIdFromAck(reqMsg);
                logger.info("got ack for " + unAckMsgId);
                synchronized (unAckMsgId) {
                    unAckRespMsgService.remove(unAckMsgId);
                    respMsgService.delUnAck(unAckMsgId);
                    logger.info("remove unack resp msg for " + unAckMsgId);
                }
                break;
            case Const.ReqMsgConst.TYPE_STRING_MSG:// only {@link Const.ReqMsgConst.TYPE_STRING_MSG/TYPE_VOICE_MSG} need ack.
            case Const.ReqMsgConst.TYPE_VOICE_MSG:
                RespMsg ackMsg = new AckRespMsg(reqMsg.getTime(), sessionService.get(sessionId).getUserId(), reqMsg.getFromData(Const.ReqMsgConst.DATA_TO_USERID, "null").toString(), reqMsg.getType());
                writeAndFlush(ctx.channel(), ackMsg);

                ctx.fireChannelRead(msg);
                break;
            case Const.ReqMsgConst.TYPE_LOGIN:
                ctx.fireChannelRead(msg);
                break;
            case Const.ReqMsgConst.TYPE_PING:
            case Const.ReqMsgConst.TYPE_LOGOUT:
            default:
                releaseObjs(reqMsg, msg);
                break;
            }
        } catch (Exception e) {
            logger.error("ack msg error.", e);
        } finally {
        }
    }

}

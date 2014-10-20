package com.sf.heros.im.handler;

import io.netty.channel.ChannelHandlerContext;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.ImUtils;
import com.sf.heros.im.common.ReqMsg;
import com.sf.heros.im.common.RespMsg;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.UnAckRespMsgService;

public class AckHandler extends CommonInboundHandler {

    private static final Logger logger = Logger.getLogger(AckHandler.class);

    private RespMsgService respMsgService;
    private UnAckRespMsgService unAckRespMsgService;

    public AckHandler(RespMsgService respMsgService,
            UnAckRespMsgService unAckRespMsgService) {
        super();
        this.respMsgService = respMsgService;
        this.unAckRespMsgService = unAckRespMsgService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (!(msg instanceof ReqMsg)) {
            super.channelRead(ctx, msg);
            return;
        }
        ReqMsg reqMsg = (ReqMsg) msg;
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
                RespMsg ackMsg = new RespMsg(Const.RespMsgConst.TYPE_ACK);
                ackMsg.setToData(Const.RespAckMsgConst.DATA_SRC_FROM_TIME, reqMsg.getTime());
                ackMsg.setToData(Const.RespAckMsgConst.DATA_MIME_USERID, reqMsg.getUserId());
                ackMsg.setToData(Const.RespAckMsgConst.DATA_SRC_TO_USERID, reqMsg.getFromData(Const.ReqMsgConst.DATA_TO_USERID, "null"));
                ackMsg.setToData(Const.RespAckMsgConst.DATA_SRC_TYPE, reqMsg.getType() + "");
                ctx.channel().writeAndFlush(ImUtils.getBuf(ctx.alloc(), ackMsg));
                break;
            case Const.ReqMsgConst.TYPE_PING:
            case Const.ReqMsgConst.TYPE_LOGOUT:
            default:
                break;
            }
        } catch (Exception e) {
            logger.error("ack msg error.", e);
        } finally {
            super.channelRead(ctx, msg);
        }
    }

}

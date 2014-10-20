package com.sf.heros.im.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.sf.heros.im.channel.TcpSocketChannel;
import com.sf.heros.im.channel.UdtSocketChannel;
import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.ReqMsg;
import com.sf.heros.im.common.RespMsg;

public class CommonInboundHandler extends ChannelInboundHandlerAdapter {

    protected String getUnAckMsgIdFromAck(ReqMsg ackMsg) {

        return ackMsg.getFromData(Const.ReqAckMsgConst.DATA_SRC_FROM_USERID, "null") + Const.CommonConst.KEY_SEP + ackMsg.getFromData(Const.ReqAckMsgConst.DATA_SRC_TO_USERID, "null") + Const.CommonConst.KEY_SEP + ackMsg.getFromData(Const.ReqAckMsgConst.DATA_SRC_FROM_TIME,"0");

    }

//    protected String getUnAckMsgId(ReqMsg reqMsg) {
//        try {
//            return reqMsg.getFromData(Const.ReqMsgConst.DATA_FROM_USERID) + Const.CommonConst.KEY_SEP + reqMsg.getFromData(Const.ReqMsgConst.DATA_TO_USERID);
//        } catch (Exception e) {
//            return null;
//        }
//    }


    protected String getUnAckMsgId(RespMsg respMsg) {
        try {
            return respMsg.getFromData(Const.RespMsgConst.DATA_KEY_FROM_USER_ID, "null") + Const.CommonConst.KEY_SEP + respMsg.getFromData(Const.RespMsgConst.DATA_KEY_TO_USER_ID, "null") + Const.CommonConst.KEY_SEP + respMsg.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    protected String getSessionId(ChannelHandlerContext ctx) {
        return getSessionId(ctx.channel());
    }

    protected String getSessionId(Channel channel) {

        if (channel instanceof TcpSocketChannel) {
            return ((TcpSocketChannel)channel).getId();
        }

        if (channel instanceof UdtSocketChannel) {
            return ((UdtSocketChannel)channel).getId();
        }

        return null;

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().flush();
        ctx.flush();
    }

}

package com.sf.heros.im.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.reflect.TypeToken;
import com.sf.heros.im.channel.util.ClientChannelIdUtil;
import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.msg.ErrAckResp;
import com.sf.heros.im.common.bean.msg.Req;

public class ReqDecoder extends ByteToMessageDecoder {

    private static final Logger logger = Logger.getLogger(ReqDecoder.class);

    private int msgBodyMaxBytes;
    private String charset;

    public ReqDecoder(int msgBodyMaxBytes, String charset) {
        super();
        this.msgBodyMaxBytes = msgBodyMaxBytes;
        this.charset = charset;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in,
            List<Object> out) throws Exception {
        int exceptMsgBodyBytes = Const.ProtocolConst.SESSION_ID_FLAG_BYTES + Const.ProtocolConst.MSG_TYPE_FLAG_BYTES + Const.ProtocolConst.MSG_BODY_FLAG_BYTES;
        if (in.readableBytes() < exceptMsgBodyBytes) {
            return;
        }
        in.markReaderIndex();
        long sid = in.readLong();
        int type = in.readInt();
        int bodyLen = in.readInt();
        if (in.readableBytes() < bodyLen) {
            logger.info(sid + " " + type + " " + bodyLen + " readable bytes " + in.readableBytes());
            in.resetReaderIndex();
            return;
        }
//        boolean discard = false;
        if (this.msgBodyMaxBytes == 0 || bodyLen > this.msgBodyMaxBytes) {
//            discard = true;
            discardMsg(in);
        }

        try {
            byte[] bodyBtys = new byte[bodyLen];
            in.readBytes(bodyBtys);
            Map<String, Object> data = Const.CommonConst.GSON.fromJson(new String(bodyBtys, charset), new TypeToken<Map<String, Object>>(){}.getType());
            Req reqMsg = new Req(sid, type, data);
//            if (discard) {
//                discardMsg(ctx, reqMsg);
//                return;
//            }
            out.add(reqMsg);
        } catch (Exception e) {
            logger.error("decode req body error.", e);
            discardMsg(in);
        }
    }


    private void discardMsg(ByteBuf in) {
        in.skipBytes(in.readableBytes());
    }


    @SuppressWarnings("unused")
    private void discardMsg(ChannelHandlerContext ctx, Req reqMsg) {

        Object msgNoObj = reqMsg.getFromData(Const.ReqConst.DATA_MSG_NO);
        if (msgNoObj != null) {
            String msgNo = msgNoObj.toString();
            ctx.channel().writeAndFlush(new ErrAckResp(ClientChannelIdUtil.getId(ctx), Const.RespConst.TYPE_MSG_TOO_LONG, msgNo));
        }


    }

}

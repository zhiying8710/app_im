package com.sf.heros.im.test.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.msg.Resp;

public class RespMsgDecoder extends ByteToMessageDecoder {

    private int sidFlagBytes;
    private int msgTypeFlagBytes;
    private int msgBodyFlagBytes;
    private int msgBodyMaxBytes;
    private int exceptMsgBodyBytes;
    private String charset;

    public RespMsgDecoder() {
        super();
        this.sidFlagBytes = 8;
        this.msgTypeFlagBytes = 4;
        this.msgBodyFlagBytes = 4;
        this.msgBodyMaxBytes = 10240;
        this.exceptMsgBodyBytes = this.sidFlagBytes + this.msgTypeFlagBytes + this.msgBodyFlagBytes;
        this.charset = "utf-8";
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in,
            List<Object> out) throws Exception {

        if (in.readableBytes() < this.exceptMsgBodyBytes) {
            return;
        }
        in.markReaderIndex();
        Long sid = in.readLong();
        int type = in.readInt();
        int bodyLen = in.readInt();
        if (in.readableBytes() < bodyLen) {
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
            Resp respMsg = new Resp(type, sid, false);
            respMsg.setData(data);
            out.add(respMsg);
        } catch (Exception e) {
            discardMsg(in);
        }
    }
    private void discardMsg(ByteBuf in) {
        in.skipBytes(in.readableBytes());
    }

}

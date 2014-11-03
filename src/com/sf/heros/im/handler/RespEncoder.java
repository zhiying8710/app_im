package com.sf.heros.im.handler;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.msg.Resp;

public class RespEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out)
            throws Exception {
        if (msg == null) {
            return;
        }
        Resp respMsg = null;
        if (msg instanceof Resp) {
            respMsg = (Resp)msg;
        } else if (msg instanceof String) {
            respMsg = Resp.fromJson(msg.toString(), Resp.class);
        }

        if (respMsg != null) {
            Long sid = respMsg.getSid();
            int type = respMsg.getType();
            out.writeLong(sid).writeInt(type);
            Map<String, Object> data = respMsg.getData();
            if (data == null || data.isEmpty()) {
                out.writeInt(0);
            } else {
                byte[] dbs = Const.CommonConst.GSON.toJson(data).getBytes("utf-8");
                out.writeInt(dbs.length);
                out.writeBytes(dbs);
            }
        }

    }

}

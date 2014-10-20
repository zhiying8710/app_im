package com.sf.heros.im.test.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import com.sf.heros.im.common.RespMsg;

public class RespMsgDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in,
            List<Object> out) throws Exception {
        int bytes = in.readableBytes();
        if (bytes > 0) {
            byte[] dst = new byte[bytes];
            in.readBytes(dst);
            try {
                RespMsg respMsg = RespMsg.fromJson(new String(dst, "utf-8"), RespMsg.class);
                out.add(respMsg);
            } catch (Exception e) {
                ctx.channel().close();
            }
        }
    }

}

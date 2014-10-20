package com.sf.heros.im.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.apache.log4j.Logger;

import com.sf.heros.im.common.ReqMsg;

public class ReqMsgDecoder extends ByteToMessageDecoder {

    private static final Logger logger = Logger.getLogger(ReqMsgDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in,
            List<Object> out) throws Exception {
        int bytes = in.readableBytes();
        if (bytes > 0) {
            byte[] dst = new byte[bytes];
            in.readBytes(dst);
            String sin = new String(dst, "utf-8");
            logger.info("decode bytes " + bytes + " for " + sin);
            try {
                ReqMsg reqMsg = ReqMsg.fromJson(sin, ReqMsg.class);
                out.add(reqMsg);
            } catch (Exception e) {
                logger.error("decode msg to ReqMsg("+ sin +") failed.", e);
//                out.add(in);
                ctx.channel().closeFuture();
            }
        }


    }

}

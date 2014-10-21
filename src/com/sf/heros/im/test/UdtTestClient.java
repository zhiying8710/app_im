package com.sf.heros.im.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sf.heros.im.test.handler.LogicMsgHandler;
import com.sf.heros.im.test.handler.PrintHandler;
import com.sf.heros.im.test.handler.RespMsgDecoder;

public class UdtTestClient {

    public void connect(final String userId, final String token, final String toUserId) {

        NioEventLoopGroup workerGroup = new NioEventLoopGroup(10, Executors.defaultThreadFactory(), NioUdtProvider.BYTE_PROVIDER);
        Bootstrap client = null;
        try {

            client = new Bootstrap();
            client.handler(new LoggingHandler(LogLevel.INFO)).channelFactory(NioUdtProvider.BYTE_CONNECTOR).group(workerGroup).handler(new ChannelInitializer<UdtChannel>() {

                @Override
                protected void initChannel(UdtChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO)).addLast(new RespMsgDecoder(), new PrintHandler(), new LogicMsgHandler(null, userId, token, toUserId));
                }
            });

            while (true) {
                ChannelFuture clientFuture = client.connect("127.0.0.1", 9000).sync();

                clientFuture.channel().closeFuture().sync();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 2; i++) {
            final int j = i;
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    new UdtTestClient().connect("1000" + j, "0000" + j, "1000" + (j == 0 ? (j + 1) : (j - 1)));

                }
            });
        }

    }

}

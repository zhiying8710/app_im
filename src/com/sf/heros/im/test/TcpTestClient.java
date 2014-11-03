package com.sf.heros.im.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.sf.heros.im.test.handler.LogicMsgHandler;
import com.sf.heros.im.test.handler.PrintHandler;
import com.sf.heros.im.test.handler.RespMsgDecoder;

public class TcpTestClient {

    public void connect(final String userId, final String token, final String toUserId) {

        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            Bootstrap client = new Bootstrap();
            client.channel(NioSocketChannel.class).group(workerGroup).option(ChannelOption.SO_SNDBUF,1048576).option(ChannelOption.SO_RCVBUF, 1048576).option(ChannelOption.SO_KEEPALIVE, true).handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new RespMsgDecoder(), new PrintHandler(), new LogicMsgHandler(null, userId, token, toUserId));
                }
            });

            ChannelFuture clientFuture = client.connect("127.0.0.1", 9000).sync();

            clientFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        int s = 2;
        ExecutorService executor = Executors.newFixedThreadPool(s);
        for (int i = 0; i < s; i++) {
            final int j = i;
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    new TcpTestClient().connect("1000" + j, "0000" + j, "1000" + (j == 0 ? (j + 1) : (j - 1)));

                }
            });
        }

    }

}

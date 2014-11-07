package com.sf.heros.im;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.sf.heros.im.channel.TcpServerSocketChannel;
import com.sf.heros.im.channel.UdtServerSocketChannel;
import com.sf.heros.im.channel.util.ClientChannelIdUtil;
import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.Counter;
import com.sf.heros.im.common.PropsLoader;
import com.sf.heros.im.common.RespPublisher;
import com.sf.heros.im.common.redis.RedisManagerV2;
import com.sf.heros.im.handler.FinalHandler;
import com.sf.heros.im.handler.ReSendUnAckRespMsgHandler;
import com.sf.heros.im.handler.ReqDecoder;
import com.sf.heros.im.handler.RespEncoder;
import com.sf.heros.im.handler.ServerHandler;
import com.sf.heros.im.service.AuthService;
import com.sf.heros.im.service.RespMsgService;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UnAckRespMsgService;
import com.sf.heros.im.service.UserInfoService;
import com.sf.heros.im.service.UserStatusService;
import com.sf.heros.im.service.impl.RedisAuthServiceImpl;
import com.sf.heros.im.service.impl.RedisRespMsgServiceImpl;
import com.sf.heros.im.service.impl.RedisSessionServiceImpl;
import com.sf.heros.im.service.impl.RedisUnAckRespMsgServiceImpl;
import com.sf.heros.im.service.impl.RedisUserInfoServiceImpl;
import com.sf.heros.im.service.impl.RedisUserStatusServiceImpl;
import com.sf.heros.im.thrift.ImCounterServer;
import com.sf.heros.im.timingwheel.UnAckRespMsgFixIntervalTimingWheel;
import com.sf.heros.im.timingwheel.service.IndicatorService;
import com.sf.heros.im.timingwheel.service.SlotKeyService;
import com.sf.heros.im.timingwheel.service.WheelService;
import com.sf.heros.im.timingwheel.service.impl.MemWheelServiceImpl;
import com.sf.heros.im.timingwheel.service.impl.RedisIndicatorServiceImpl;
import com.sf.heros.im.timingwheel.service.impl.SlotKeyServiceImpl;

public class AppMain {

    private static final Logger logger = Logger.getLogger(AppMain.class);

    public AppMain() {

        init();

        String servType = PropsLoader.get(Const.PropsConst.SERVER_TYPE, Const.PropsConst.SERVER_TYPE_DEAFULT);
        logger.info("server type is " + servType);
        NioEventLoopGroup bossGroup = null;
        NioEventLoopGroup workerGroup = null;
        ServerBootstrap server = new ServerBootstrap();
        logger.info("create ServerBootstrap.");
        if (servType.equals(Const.PropsConst.SERVER_TYPE_DEAFULT)) {
            bossGroup = new NioEventLoopGroup(PropsLoader.get(Const.PropsConst.BOSS_GROUP_THREADS, 5));
            workerGroup = new NioEventLoopGroup(PropsLoader.get(Const.PropsConst.WORKER_GROUP_THREADS, 100));
            server.channel(TcpServerSocketChannel.class)
                  .childOption(ChannelOption.SO_KEEPALIVE, true)
                  .childOption(ChannelOption.TCP_NODELAY,true);
        } else if (servType.equals(Const.PropsConst.SERVER_TYPE_UDT)) {
            bossGroup = new NioEventLoopGroup(PropsLoader.get(Const.PropsConst.BOSS_GROUP_THREADS, 5),
                    Executors.defaultThreadFactory(), NioUdtProvider.BYTE_PROVIDER);
            workerGroup = new NioEventLoopGroup(PropsLoader.get(Const.PropsConst.WORKER_GROUP_THREADS, 100),
                    Executors.defaultThreadFactory(), NioUdtProvider.BYTE_PROVIDER);
            server.channel(UdtServerSocketChannel.class);
        } else {
            logger.warn("unsupport server type, system exit.");
            System.exit(0);
        }
        server.childOption(ChannelOption.SO_REUSEADDR,true)     //重用地址
              .childOption(ChannelOption.ALLOCATOR,new PooledByteBufAllocator(true))
              .childOption(ChannelOption.SO_RCVBUF, 1048576)
              .childOption(ChannelOption.SO_SNDBUF,1048576)
              .handler(new LoggingHandler(LogLevel.INFO))
              .option(ChannelOption.SO_BACKLOG, PropsLoader.get(Const.PropsConst.SERVER_SOCKET_BACKLOG_COUNT, 100))
              .group(bossGroup, workerGroup);
        logger.info("init boss and worker event loop group, and init channel type, parent channel options, child channel options.");

        boot(server);
    }

    private void init() {
        PropsLoader.load();
        String channelIdThriftHost = PropsLoader.get(Const.PropsConst.CHANNEL_ID_THRIFT_HOST, "");
        int channelIdThriftPort = PropsLoader.get(Const.PropsConst.CHANNEL_ID_THRIFT_PORT, -1);
        if (channelIdThriftHost.equals("") || channelIdThriftPort == -1) {
            logger.warn("channel's id is based on snowflake, if threr is't a thrift server offered, use local idworker.");
            ClientChannelIdUtil.useLocalIdWorker();
        }
        RedisManagerV2.getInstance();

        Counter.ConnsCounter.INSTANCE.init();
        Counter.OnlinesCounter.INSTANCE.init();

        Executors.newSingleThreadExecutor().submit(new Runnable() {

            @Override
            public void run() {
                try {
                    new ImCounterServer().boot(PropsLoader.get(Const.PropsConst.IM_COUNTER_THRIFT_SERVER_PORT, 19000));
                } catch (Exception e) {
                    logger.error("im counter server run error.", e);
                }
            }
        });
    }

    public void boot(ServerBootstrap server) {

        try {

            final AuthService authService = new RedisAuthServiceImpl();
            final UserStatusService userStatusService = new RedisUserStatusServiceImpl();
//            final SessionService sessionService = new MemSessionServiceImpl();
            final SessionService sessionService = new RedisSessionServiceImpl();
            final UserInfoService userInfoService = new RedisUserInfoServiceImpl();
            final RespMsgService respMsgService = new RedisRespMsgServiceImpl();
            WheelService wheel = new MemWheelServiceImpl();
            IndicatorService indicator = new RedisIndicatorServiceImpl();
            SlotKeyService slotKeyService = new SlotKeyServiceImpl();
//            final UnAckRespMsgService unAckRespMsgService = new MemUnAckRespMsgServiceImpl(new UnAckRespMsgFixIntervalTimingWheel(
//                    PropsLoader.get(Const.PropsConst.UN_ACK_RESP_MSG_WHEEL_DURATION_SECS, 3),
//                    PropsLoader.get(Const.PropsConst.UN_ACK_RESP_MSG_WHEEL_PER_SLOT_SECS, 1), TimeUnit.SECONDS,
//                    PropsLoader.get(Const.PropsConst.UN_ACK_RESP_MSG_WHEEL_NAME, "un_ack_msg_wheel"), wheel, indicator, slotKeyService, respMsgService));

            final UnAckRespMsgService unAckRespMsgService = new RedisUnAckRespMsgServiceImpl(new UnAckRespMsgFixIntervalTimingWheel(
                    PropsLoader.get(Const.PropsConst.UN_ACK_RESP_MSG_WHEEL_DURATION_SECS, 3),
                    PropsLoader.get(Const.PropsConst.UN_ACK_RESP_MSG_WHEEL_PER_SLOT_SECS, 1), TimeUnit.SECONDS,
                    PropsLoader.get(Const.PropsConst.UN_ACK_RESP_MSG_WHEEL_NAME, "un_ack_msg_wheel"), wheel, indicator, slotKeyService, respMsgService));


            final ReSendUnAckRespMsgHandler reSendUnAckRespMsgHandler = new ReSendUnAckRespMsgHandler(
                    sessionService, userStatusService, respMsgService, unAckRespMsgService,
                    PropsLoader.get(Const.PropsConst.RE_SEND_UN_ACK_POOL_SIZE, 5));

            final FinalHandler finalHandler = new FinalHandler();

            final ServerHandler serverHandler = new ServerHandler(authService, sessionService, userStatusService, userInfoService, respMsgService, unAckRespMsgService);

//            final PrintHandler printHandler = new PrintHandler();

            RespPublisher.init(sessionService, userStatusService);

            server.childHandler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel ch)
                                throws Exception {
                            ch.pipeline()
//                                    .addLast(Const.HandlerConst.HANDLER_RESP_MSG_SUB_NAME, new RespMsgSubHandler())
                                    .addLast(Const.HandlerConst.HANDLER_RE_SEND_UN_ACK_NAME, reSendUnAckRespMsgHandler)
                                    .addLast(Const.HandlerConst.HANDLER_MSG_DECODER_NAME, new ReqDecoder(Const.ProtocolConst.MSG_BODY_MAX_BYTES,
                                                                                                         Const.ProtocolConst.DEFAULT_CHARSET))
//                                    .addLast(Const.HandlerConst.HANDLER_PRINT_NAME, printHandler)
                                    .addLast(Const.HandlerConst.HANDLER_IDLE_STATE_CHECK_NAME,
                                            new IdleStateHandler(PropsLoader.get(Const.PropsConst.CHANNEL_READ_IDLE_SECS, 10),
                                                                 PropsLoader.get(Const.PropsConst.CHANNEL_WRITE_IDLE_SECS, 5),
                                                                 PropsLoader.get(Const.PropsConst.CHANNEL_ALL_IDLE_SECS, 15),
                                                                 TimeUnit.SECONDS))
                                    .addLast(Const.HandlerConst.HANDLER_LOGIC_EVENT_NAME, serverHandler)
                                    .addLast(finalHandler)
                                    .addLast(Const.HandlerConst.HANDLER_RESP_ENCODER_NAME, new RespEncoder());
                        }
                    });
            logger.info("inited ServerBootstrap.");
            String servHost = PropsLoader.get(Const.PropsConst.IM_HOST, "127.0.0.1");
            int servPort = PropsLoader.get(Const.PropsConst.IM_PORT, 9000);
            ChannelFuture bindFuture = server.bind(servHost, servPort).sync();
            logger.info("bound ServerBootstrap to " + servHost + ":" + servPort + ", app booted.");

            bindFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error occured.", e);
        } finally {

            class AppShutdownHook implements Runnable {

                private List<EventLoopGroup> groups = new ArrayList<EventLoopGroup>();

                public AppShutdownHook(EventLoopGroup... groups) {
                    for (EventLoopGroup group : groups) {
                        this.groups.add(group);
                    }
                }

                @Override
                public void run() {
                    for (EventLoopGroup group : this.groups) {
                        if (group != null && !group.isShutdown()) {
                            group.shutdownGracefully();
                        }
                    }
                    logger.info("shutdown groups gracefully and app is shutdown.");
                }

            }
            if (server != null) {
                Runtime.getRuntime().addShutdownHook(new Thread(new AppShutdownHook(server.childGroup(), server.group())));
            }

            System.exit(0);
        }

    }

    public static void main(String[] args) {

        new AppMain();

    }

}

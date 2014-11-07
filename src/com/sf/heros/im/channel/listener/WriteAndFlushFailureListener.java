package com.sf.heros.im.channel.listener;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import org.apache.log4j.Logger;

import com.sf.heros.im.channel.ClientChannel;
import com.sf.heros.im.common.bean.Session;
import com.sf.heros.im.service.SessionService;
import com.sf.heros.im.service.UserStatusService;

public class WriteAndFlushFailureListener implements ChannelFutureListener {

	private static final Logger logger = Logger.getLogger(WriteAndFlushFailureListener.class);

	private UserStatusService userStatusService;
	private SessionService sessionService;

	public WriteAndFlushFailureListener(SessionService sessionService,
			UserStatusService userStatusService) {
		this.userStatusService = userStatusService;
		this.sessionService = sessionService;
	}

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		if (!future.isSuccess()) {
			logger.error("channel writeAndFlush failed, close it.");
			Channel ch = future.channel();
			if (ch == null) {
				return;
			}
			if (ch instanceof ClientChannel) {
				ClientChannel clientChannel = (ClientChannel)ch;
				clientChannel.ungroup();
				Session session = this.sessionService.get(clientChannel.getId());
				if (session != null && session.getUserId() != null) {
					this.userStatusService.userOffline(session.getUserId());
				}
			}
			ch.close();
        }
	}

}

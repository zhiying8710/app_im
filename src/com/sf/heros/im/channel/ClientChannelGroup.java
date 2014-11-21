package com.sf.heros.im.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.ChannelMatchers;
import io.netty.util.ReferenceCountUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientChannelGroup {

    private static final String name = "group-client-channels";
    private static final Map<Long, ClientChannel> channels = new ConcurrentHashMap<Long, ClientChannel>();

    public static int size() {
        return channels.size();
    }

    public static boolean isEmpty() {
        return channels.isEmpty();
    }

    public static boolean contains(ClientChannel c) {
        if (c == null) {
            return false;
        }
        return channels.containsValue(c);
    }

    public static boolean add(ClientChannel c) {
        if (c == null) {
            throw new NullPointerException("c can't be null.");
        }
        channels.put(c.getId(), c);
        return true;
    }

    public static boolean remove(ClientChannel c) {
        if (c == null) {
            return true;
        }
        channels.remove(c.getId());
        return true;
    }


    public void clear() {
        channels.clear();
    }


    public static String name() {
        return name;
    }

    public static void write(Object message) {
        write(message, ChannelMatchers.all());
    }

    // Create a safe duplicate of the message to write it to a channel but not affect other writes.
    // See https://github.com/netty/netty/issues/1461
    private static Object safeDuplicate(Object message) {
        if (message instanceof ByteBuf) {
            return ((ByteBuf) message).duplicate().retain();
        } else if (message instanceof ByteBufHolder) {
            return ((ByteBufHolder) message).duplicate().retain();
        } else {
            return ReferenceCountUtil.retain(message);
        }
    }

    public static void write(Object message, ChannelMatcher matcher) {
        if (message == null) {
            throw new NullPointerException("message");
        }
        if (matcher == null) {
            throw new NullPointerException("matcher");
        }

        for (ClientChannel c: channels.values()) {
            if (matcher.matches(c)) {
                c.write(safeDuplicate(message));
            }
        }
        ReferenceCountUtil.release(message);
    }

    public static void flush() {
        flush(ChannelMatchers.all());;
    }

    public static void flush(ChannelMatcher matcher) {
        if (matcher == null) {
            throw new NullPointerException("matcher");
        }

        for (ClientChannel c: channels.values()) {
            if (matcher.matches(c)) {
                c.flush();
            }
        }

    }

    public static void writeAndFlush(Object message) {
        writeAndFlush(message, ChannelMatchers.all());
    }

    public static void flushAndWrite(Object message) {
        writeAndFlush(message);
    }

    public static void writeAndFlush(Object message,
            ChannelMatcher matcher) {
        if (message == null) {
            throw new NullPointerException("message");
        }

        for (ClientChannel c: channels.values()) {
            if (matcher.matches(c)) {
                c.writeAndFlush(safeDuplicate(message));
            }
        }

        ReferenceCountUtil.release(message);
    }

    public static void flushAndWrite(Object message,
            ChannelMatcher matcher) {
        writeAndFlush(message, matcher);
    }

    public static void disconnect() {
        disconnect(ChannelMatchers.all());
    }

    public static void disconnect(ChannelMatcher matcher) {
        if (matcher == null) {
            throw new NullPointerException("matcher");
        }

        for (ClientChannel c: channels.values()) {
            if (matcher.matches(c)) {
                c.disconnect();
            }
        }
    }

    public static void close() {
        close(ChannelMatchers.all());
    }

    public static void close(ChannelMatcher matcher) {
        if (matcher == null) {
            throw new NullPointerException("matcher");
        }


        for (ClientChannel c: channels.values()) {
            if (matcher.matches(c)) {
                c.close();
            }
        }
    }

    public static void deregister() {
        deregister(ChannelMatchers.all());
    }

    public static void deregister(ChannelMatcher matcher) {
        if (matcher == null) {
            throw new NullPointerException("matcher");
        }

        for (ClientChannel c: channels.values()) {
            if (matcher.matches(c)) {
                c.deregister();
            }
        }
    }

    public static ClientChannel get(Long channelId) {
        return channels.get(channelId);
    }

    public static void close(Long channelId) {
        ClientChannel clientChannel = get(channelId);
        if (clientChannel != null) {
            clientChannel.close();
        }


    }

}

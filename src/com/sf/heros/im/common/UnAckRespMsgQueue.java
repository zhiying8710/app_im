package com.sf.heros.im.common;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class UnAckRespMsgQueue {

    private static final LinkedBlockingDeque<String> UN_ACK_RESP_MSG_QUEUE = new LinkedBlockingDeque<String>(1000);

    public static boolean addLast(String unAckRespMsg) {
        return UN_ACK_RESP_MSG_QUEUE.add(unAckRespMsg);
    }

    public static String getFirst() {
        try {
            return UN_ACK_RESP_MSG_QUEUE.poll(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }


}

package com.sf.heros.im.common;


import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sf.heros.im.common.exception.ProtocolException;

public class Const {

    public interface ProtocolConst {

        public static final int SESSION_ID_FLAG_BYTES = 8;
        public static final int MSG_TYPE_FLAG_BYTES = 4;
        public static final int MSG_BODY_FLAG_BYTES = 4;
        public static final int MSG_BODY_MAX_BYTES = 10240;

        public static final String DEFAULT_CHARSET = "utf-8";

        public static final Long EMPTY_SESSION_ID = 0L;

        enum FlagType {
            BYTE, SHORT, INT, LONG;
            public int len() {
                if (this == BYTE) {
                    return 1;
                }
                if (this == SHORT) {
                    return 2;
                }
                if (this == INT) {
                    return 4;
                }
                if (this == LONG) {
                    return 8;
                }
                throw new ProtocolException("unsupport flag type.");
            }
        }
    }

    public interface ReqConst {

        public static final int TYPE_PING = 0;
        public static final int TYPE_STRING_MSG = 1;
        public static final int TYPE_VOICE_MSG = 2;
        public static final int TYPE_LOGOUT = 4;
        public static final int TYPE_ACK = 5;
        public static final int TYPE_LOGIN = 12;


        public static final String DATA_TO_USERID = "to";
        public static final String DATA_FROM_USERID = "from";
        public static final String DATA_CONTENT = "content";
        public static final String DATA_MSG_NO = "msg_no";

        public static final String DATA_AUTH_USERID = "userId";
        public static final String DATA_AUTH_TOKEN = "token";

        public static final String TIME = "time";
    }

    public interface ReqAckConst {

        public static final String DATA_SRC_FROM_USERID = "src_from";
        public static final String DATA_SRC_TO_USERID = "src_to";
        public static final String DATA_SRC_MSG_NO = "src_msg_no";

    }

    public interface RespConst {

        public static final int TYPE_STRING_MSG = 1;
        public static final int TYPE_VOICE_MSG = 2;
        public static final int TYPE_ACK = 5;
        public static final int TYPE_KICKED = 6;
        public static final int TYPE_MSG_HANDLER_ERROR = 7;
        public static final int TYPE_REQ_PING = 8;
        public static final int TYPE_SERVER_ERR = 9;
        public static final int TYPE_OFFLINE_MSG = 10;
        public static final int TYPE_ASK_LOGIN = 11;
        public static final int TYPE_LOGIN = 12;
        public static final int TYPE_MSG_TOO_LONG = 13;
        public static final int TYPE_AUTH_ERR = 14;

        public static final String DATA_KEY_CONTENT = "content";
        public static final String DATA_KEY_FROM_USER_ID = "from";
        public static final String DATA_KEY_FROM_USER_INFO = "from_info";
        public static final String DATA_KEY_OFFLINE_MSGS = "offline_msgs";
        public static final String DATA_KEY_TO_USER_ID = "to";
        public static final String DATA_KEY_MSG_NO = "msg_no";
    }

    public interface RespAckConst {

        public static final String DATA_SRC_TO_USERID = "src_to";
        public static final String DATA_SRC_FROM_USERID = "src_from";
        public static final String DATA_SRC_MSG_NO = "src_msg_no";
        public static final String DATA_KEY_REMARK = "remark";

    }

    public interface HandlerConst {

        public static final String HANDLER_MSG_DECODER_NAME = "reqMsgDecoder";
        public static final String HANDLER_PRINT_NAME = "print";
        public static final String HANDLER_IDLE_STATE_CHECK_NAME = "idleStateCheck";
        public static final String HANDLER_HEARTBEAT_NAME = "heartBeat";
        public static final String HANDLER_ACK_NAME = "ack";
        public static final String HANDLER_AUTH_NAME = "auth";
        public static final String HANDLER_LOGIC_NAME = "logic";
        public static final String HANDLER_RE_SEND_UN_ACK_NAME = "reSendUnAck";
        public static final String HANDLER_RESP_MSG_SUB_NAME = "respMsgSub";
        public static final String HANDLER_LOGIC_EVENT_NAME = "loginEvent";
        public static final String HANDLER_RESP_ENCODER_NAME = "respEncoder";

    }

    public interface PropsConst {

        public static final String REDIS_POOL_TYPE = "redis.pool.type";
        public static final String REDIS_DF_CONNS = "redis.default.conns";
        public static final String REDIS_DF_DB = "redis.df_db";
        public static final String REDIS_TIMEOUT = "redis.timeout";
        public static final String REDIS_SINGLE_HOST = "redis.single.host";
        public static final String REDIS_SINGLE_PORT = "redis.single.port";
        public static final String REDIS_SENTINEL_MASTER_NAME = "reis.sentinel.master.name";
        public static final String REDIS_SENTINEL_ADDRS = "redis.sentinel.addrs";

        public static final String IM_HOST = "im.host";
        public static final String IM_PORT = "im.port";
        public static final String PING_OVERTIME = "im.client.ping.overtime";
        public static final String CHANNEL_ALL_IDLE_SECS = "im.all.idle.secs";
        public static final String CHANNEL_READ_IDLE_SECS = "im.read.idle.secs";
        public static final String CHANNEL_WRITE_IDLE_SECS = "im.write.idle.secs";
        public static final String RE_SEND_UN_ACK_POOL_SIZE = "im.re.send.un.ack.pool.size";
        public static final String UN_ACK_RESP_MSG_WHEEL_DURATION_SECS = "im.un.ack.resp.msg.wheel.duration.secs";
        public static final String UN_ACK_RESP_MSG_WHEEL_PER_SLOT_SECS = "im.un.ack.resp.msg.wheel.per.slot.secs";
        public static final String UN_ACK_RESP_MSG_WHEEL_NAME = "im.un.ack.resp.msg.wheel.name";
        public static final String AUTH_CHECK_SO_ILLAG = "im.auth.check.so.illegal";
        public static final String SERVER_TYPE = "im.server.type";
        public static final String SERVER_TYPE_DEAFULT = "tcp";
        public static final String SERVER_TYPE_UDT = "udt";
        public static final String WORKER_GROUP_THREADS = "im.server.worker.threads";
        public static final String SERVER_SOCKET_BACKLOG_COUNT = "im.server.socket.backlog.count";
        public static final String BOSS_GROUP_THREADS = "im.server.boss.threads";

//        public static final String SERVER_NAME = "im.server.name";
        public static final String SERVER_ID = "im.server.id";

        public static final String CHANNEL_ID_THRIFT_HOST = "im.channel.id.thrift.host";
        public static final String CHANNEL_ID_THRIFT_PORT = "im.channel.id.thrift.port";
        public static final String CHANNEL_ID_THRIFT_USRAGENT = "im.channel.id.thrift.usragent";
        public static final String IM_COUNTER_THRIFT_SERVER_PORT = "im.counter.thrift.server.port";
        public static final String UNACKMSG_RESEND_COUNT = "im.unackmsg.resend.count";
		public static final String SERVER_UNIQUE_ID = "im.server.unique.id";

    }

    public interface RedisConst {

        public static final String USER_STATUS_KEY_PRIFIX = "__user_status_";
        public static final String USER_STATUS_KEY_TOKEN = "token";
        public static final String USER_STATUS_KEY_SO_ONLINE = "so_online";
        public static final String USER_STATUS_KEY_SO_SESSION_ID = "so_session_id";
        public static final String USER_STATUS_KEY_SO_LOGIN_TIME = "so_login_time";
        public static final Object USER_SATATUS_KEY_ONLINE = "online";

        public static final String USER_STATUS_VAL_SO_ONLINE_OFFLINE = "0";
        public static final String USER_STATUS_VAL_SO_ONLINE_ONLINE = "1";
        public static final String USER_STATUS_VAL_ONLINE_ONLINE = "1";


        public static final String RESP_MSG_UNACK_KEY = "__resp_msg_unack";

        public static final String SINGEL_ERR_VAL = "\001single_err_val";

        public static final String USER_INFO_KEY_PREFIX = "__user_info_";
        public static final String USER_INFO_KEY_HEAD = "head";
        public static final String USER_INFO_KEY_NICKNAME = "nickname";

        public static final String USER_OFFLINE_MSG_KEY_PREFIX = "__user_offline_msg_";
        public static final String RESP_MSG_SUB_KEY_PREFIX = "__resp_msg_sub_";
        public static final String SESSION_KEY_PREFIX = "__session_";
        public static final String RESP_MSG_UNACK_RESEND_KEY = "__resp_msg_unack_resend";
		public static final String UNACKMSG_RESEND_COUNT_KEY = "__unackmsg_resend_count";
    }

    public interface UserConst {

        public static final String INFO_KEY_HEAD_VAL = null;
        public static final String INFO_KEY_NICKNAME_DF_VAL = "John Doe";

    }


    public interface CommonConst {

        public static final int HEART_BEAT_INTERVAL_SECS = 10;
        public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
//        public static final String SESSION_ID_KEY_NAME = "_id";
        public static final String KEY_SEP = "_";
        public static final int OFFLINE_MSG_SEND_PER_SIZE = 20;
        public static final String SERVER_USER_ID = "server";
        public static final int SESSION_TIMEOUT_SECS = 60 * 30;
        public static final Random RANDOM = new Random(10);
    }

}

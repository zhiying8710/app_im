package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;

public abstract class ChatResp extends Resp {

    private static final long serialVersionUID = 1L;

    public ChatResp(int type, Long sid, boolean needMsgNo) {
        super(type, sid, needMsgNo);
    }

    public ChatResp(Long sid, boolean needMsgNo) {
        super(sid, needMsgNo);
    }

    public String getTo() {
        return getFromData(Const.RespConst.DATA_KEY_TO_USER_ID).toString();
    }

}

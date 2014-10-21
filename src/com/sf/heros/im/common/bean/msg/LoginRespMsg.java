package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;

public class LoginRespMsg extends RespMsg {

    private static final long serialVersionUID = 1L;

    public LoginRespMsg(String sessionId) {
        setType(Const.RespMsgConst.TYPE_LOGIN);
        setSessionId(sessionId);
    }

    private void setSessionId(String sessionId) {
        setToData(Const.RespMsgConst.DATA_KEY_LOGIN_SESSIONID, sessionId);
    }

}

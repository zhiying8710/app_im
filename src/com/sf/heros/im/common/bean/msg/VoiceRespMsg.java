package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.UserInfo;

public class VoiceRespMsg extends RespMsg {

    private static final long serialVersionUID = 1L;

    public VoiceRespMsg(String content, String from, UserInfo fromInfo, String to) {
        setType(Const.RespMsgConst.TYPE_VOICE_MSG);
        setContent(content);
        setFrom(from);
        setFromInfo(fromInfo);
        setTo(to);
    }

    private void setContent(String content) {
        setToData(Const.RespMsgConst.DATA_KEY_CONTENT, content);
    }

    private void setFrom(String from) {
        setToData(Const.RespMsgConst.DATA_KEY_FROM_USER_ID, from);
    }

    private void setFromInfo(UserInfo fromInfo) {
        setToData(Const.RespMsgConst.DATA_KEY_FROM_USER_INFO, fromInfo);
    }

    private void setTo(String to) {
        setToData(Const.RespMsgConst.DATA_KEY_TO_USER_ID, to);
    }
}

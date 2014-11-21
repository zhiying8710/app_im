package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.bean.UserInfo;

public class VoiceResp extends ChatResp {

    private static final long serialVersionUID = 1L;

    public VoiceResp(Long sid, String content, String from, UserInfo fromInfo, String to) {
        super(sid, true);
        setType(Const.RespConst.TYPE_VOICE_MSG);
        setContent(content);
        setFrom(from);
        setFromInfo(fromInfo);
        setTo(to);
        setTime();
    }

    private void setContent(String content) {
        setToData(Const.RespConst.DATA_KEY_CONTENT, content);
    }

    private void setFrom(String from) {
        setToData(Const.RespConst.DATA_KEY_FROM_USER_ID, from);
    }

    private void setFromInfo(UserInfo fromInfo) {
        setToData(Const.RespConst.DATA_KEY_FROM_USER_INFO, fromInfo);
    }

    private void setTo(String to) {
        setToData(Const.RespConst.DATA_KEY_TO_USER_ID, to);
    }

    public String getTo() {
        return getFromData(Const.RespConst.DATA_KEY_TO_USER_ID).toString();
    }
}

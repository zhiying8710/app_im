package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;

public class AskLoginResp extends Resp {

    private static final long serialVersionUID = 1L;

    public AskLoginResp() {
        super(Const.ProtocolConst.EMPTY_SESSION_ID, false);
        setType(Const.RespConst.TYPE_ASK_LOGIN);
    }

}

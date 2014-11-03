package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;

public class ServErrResp extends Resp {

    private static final long serialVersionUID = 1L;

    public ServErrResp(Long sid) {
        super(sid, false);
        setType(Const.RespConst.TYPE_SERVER_ERR);
    }

}

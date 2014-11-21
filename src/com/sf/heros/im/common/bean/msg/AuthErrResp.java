package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;

public class AuthErrResp extends Resp {

	private static final long serialVersionUID = 1L;

	public AuthErrResp() {
		super(Const.ProtocolConst.EMPTY_SESSION_ID, false);
        setType(Const.RespConst.TYPE_AUTH_ERR);
	}


}

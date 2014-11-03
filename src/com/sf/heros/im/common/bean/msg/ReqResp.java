package com.sf.heros.im.common.bean.msg;

import com.sf.heros.im.common.Const;

public abstract class ReqResp {

    public String toJson() {
        return Const.CommonConst.GSON.toJson(this, this.getClass());
    }

    public static <E> E fromJson(String json, Class<E> clz) {
        return Const.CommonConst.GSON.fromJson(json, clz);
    }

}

package com.sf.heros.im.common;

public abstract class Msg {

    public String toJson() {
        return Const.CommonConst.GSON.toJson(this, this.getClass());
    }

    public static <E> E fromJson(String json, Class<E> clz) {
        return Const.CommonConst.GSON.fromJson(json, clz);
    }

}

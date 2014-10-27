package com.sf.heros.im.common.bean.msg;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sf.heros.im.common.Const;

public class RespMsg extends Msg implements Serializable {

    private static final long serialVersionUID = 1L;
    private int type;
    private String time = new Date().getTime() + "";
    private Map<String, Object> data;

    public RespMsg() {
    }

    public RespMsg(int type) {
        super();
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setToData(String key, Object val) {
        if (data == null) {
            data = new HashMap<String, Object>();
        }
        data.put(key, val);
    }

    public Object getFromData(String key, Object defaultVal) {
        if (data == null) {
            return defaultVal;
        }
        Object val = data.get(key);
        return val == null ? defaultVal : val;
    }

    public Object getFromData(String key) {
        if (data == null) {
            return null;
        }
        return data.get(key);
    }

    public String getUnAckMsgId() {
        try {
            return this.getFromData(Const.RespMsgConst.DATA_KEY_FROM_USER_ID, "null") + Const.CommonConst.KEY_SEP + this.getFromData(Const.RespMsgConst.DATA_KEY_TO_USER_ID, "null") + Const.CommonConst.KEY_SEP + this.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "RespMsg [type=" + type + ", time=" + time + ", data=" + data
                + "]";
    }

}

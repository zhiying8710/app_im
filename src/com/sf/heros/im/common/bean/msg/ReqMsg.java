package com.sf.heros.im.common.bean.msg;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReqMsg extends Msg implements Serializable {
    private static final long serialVersionUID = 1L;

    private int type;
    private Map<String, Object> data;
    private String time = new Date().getTime() + "";
    private String sid;

    public ReqMsg() {
    }

    public ReqMsg(String sid) {
        super();
        this.sid = sid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public Object getFromData(String key, Object defaultVal) {
        if (data == null) {
            return defaultVal;
        }
        Object val = data.get(key);
        return val == null ? defaultVal : val;
    }

    @Override
    public String toString() {
        return "ReqMsg [type=" + type + ", data=" + data + ", time=" + time
                + ", sid=" + sid + "]";
    }

    public void setToData(String key, Object val) {
        if(data == null) {
            data = new HashMap<String, Object>();
        }
        data.put(key, val);
    }

    public Object getFromData(String key) {
        if (data == null) {
            return null;
        }
        return data.get(key);
    }
}

package com.sf.heros.im.common;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RespMsg extends Msg implements Serializable {

    @Override
    public String toString() {
        return "RespMsg [type=" + type + ", time=" + time + ", data=" + data
                + "]";
    }

    private static final long serialVersionUID = 1L;
    private int type;
    private String time = new Date().getTime() + "";
    private Map<String, Object> data = new HashMap<String, Object>();

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

    public void setTime(String time) {
        this.time = time;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
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

}

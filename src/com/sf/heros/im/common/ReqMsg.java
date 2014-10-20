package com.sf.heros.im.common;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReqMsg extends Msg implements Serializable {
    private static final long serialVersionUID = 1L;

    private int type;
    private Map<String, Object> data;
    private String time = new Date().getTime() + "";
    private String userId;
    private String token;

    public ReqMsg() {
    }

    public ReqMsg(String userId, String token) {
        super();
        this.userId = userId;
        this.token = token;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
                + ", userId=" + userId + ", token=" + token + "]";
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

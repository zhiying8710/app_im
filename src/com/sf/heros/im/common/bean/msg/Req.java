package com.sf.heros.im.common.bean.msg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Req extends ReqResp implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long sid;
    private int type;
    private Map<String, Object> data;

    public Req(Long sid, int type, Map<String, Object> data) {
        super();
        this.sid = sid;
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getSid() {
        return sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Object getFromData(String key, Object defaultVal) {
        if (data == null) {
            return defaultVal;
        }
        Object val = data.get(key);
        return val == null ? defaultVal : val;
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

    @Override
    public String toString() {
        return "Req [sid=" + sid + ", type=" + type + ", data=" + data + "]";
    }

}

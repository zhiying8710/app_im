package com.sf.heros.im.common.bean.msg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sf.heros.im.common.Const;
import com.sf.heros.im.common.ImUtils;
import com.sf.heros.im.common.PropsLoader;

public class Resp extends ReqResp implements Serializable {

    private static final long serialVersionUID = 1L;
    private int type;
    private Long sid;
    private Map<String, Object> data;
    private static final String serverId = PropsLoader.get(Const.PropsConst.SERVER_ID);

    protected Resp(Long sid, boolean needMsgNo) {
        super();
        this.sid = sid;
        if (needMsgNo) {
            setToData(Const.RespConst.DATA_KEY_MSG_NO, ImUtils.getUniqueId(sid + serverId));
        }
    }

    public Resp(int type, Long sid, boolean needMsgNo) {
        this(sid, needMsgNo);
        this.type = type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getMsgNo() {
        return getFromData(Const.RespConst.DATA_KEY_MSG_NO, "0000").toString();
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

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    protected void setTime() {
        setToData(Const.ReqConst.TIME, System.currentTimeMillis() + "");
    }

    @Override
    public String toString() {
        return "Resp [type=" + type + ", sid=" + sid + ", data=" + data + "]";
    }

}

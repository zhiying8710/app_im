package com.sf.heros.im.common;

public class UserInfo {

    private String id;
    private String head = Const.UserConst.INFO_KEY_HEAD_VAL;
    private String nickName = Const.UserConst.INFO_KEY_NICKNAME_DF_VAL;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

}

package com.sf.heros.im.common.bean;

public class AuthCheck {

    private boolean isPass;
    private boolean isOnline;
    private boolean isIllegal;

    public boolean isPass() {
        return isPass;
    }

    public void setPass(boolean isPass) {
        this.isPass = isPass;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isIllegal() {
        return isIllegal;
    }

    public void setIllegal(boolean isIllegal) {
        this.isIllegal = isIllegal;
    }

    @Override
    public String toString() {
        return "AuthCheck [isPass=" + isPass + ", isOnline=" + isOnline
                + ", isIllegal=" + isIllegal + "]";
    }

}

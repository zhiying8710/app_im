package com.sf.heros.im.common.redis;


public class RedisCmdPair {

    private String cmd;
    private Object[] oArgs;

    public RedisCmdPair() {
    }

    public RedisCmdPair(String cmd, Object[] oArgs) {
        this.cmd = cmd;
        this.oArgs = oArgs;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Object[] getoArgs() {
        return oArgs;
    }

    public void setoArgs(Object[] oArgs) {
        this.oArgs = oArgs;
    }
}

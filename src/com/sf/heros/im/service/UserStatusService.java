package com.sf.heros.im.service;

public interface UserStatusService {

    public boolean userOnline(String userId, String token, String sessionId, long loginTime);

    public boolean userOnline(String userId, String token, String sessionId);

    public void userOffline(String userId);

    public String getSessionId(String userId);

    public boolean isOnline(String userId);

    public void offlineAll();

}

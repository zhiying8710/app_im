package com.sf.heros.im.service;

public interface UserStatusService {

    public boolean userOnline(String userId, String token, Long sessionId, long loginTime);

    public boolean userOnline(String userId, String token, Long sessionId);

    public void userOffline(String userId);

    public Long getSessionId(String userId);

    public boolean isOnline(String userId);

    public void offlineAll();

}

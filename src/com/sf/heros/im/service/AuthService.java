package com.sf.heros.im.service;

import com.sf.heros.im.common.AuthCheck;

public interface AuthService {

    public AuthCheck check(String userId, String token);

}

package com.qprogramming.shopper.app.login.token;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Jakub Romaniszyn on 19.07.2018.
 * <p>
 * Based on
 * https://github.com/bfwg/springboot-jwt-starter
 */
@Getter
@Setter
public class UserTokenState {
    private String access_token;
    private Long expires_in;

    public UserTokenState() {
        this.access_token = null;
        this.expires_in = -1L;
    }

    public UserTokenState(String access_token, long expires_in) {
        this.access_token = access_token;
        this.expires_in = expires_in;
    }
}

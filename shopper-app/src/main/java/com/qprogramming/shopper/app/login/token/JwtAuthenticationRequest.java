package com.qprogramming.shopper.app.login.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Jakub Romaniszyn on 2018-10-29
 */
@AllArgsConstructor
@Getter
@Setter
public class JwtAuthenticationRequest {
    private String username;
    private String password;

    public JwtAuthenticationRequest() {
        super();
    }
}

package com.qprogramming.shopper.app.login;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Created by Jakub Romaniszyn on 19.07.2018.
 *
 * Based on
 * https://github.com/bfwg/springboot-jwt-starter
 */
public class AnonAuthentication extends AbstractAuthenticationToken {

    public AnonAuthentication() {
        super(null);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }


}

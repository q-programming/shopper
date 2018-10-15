package com.qprogramming.shopper.app.login.token;

import com.qprogramming.shopper.app.account.Account;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Created by Jakub Romaniszyn on 19.07.2018.
 * <p>
 * Based on
 * https://github.com/bfwg/springboot-jwt-starter
 */
public class TokenBasedAuthentication extends AbstractAuthenticationToken {

    private final UserDetails principal;
    private String token;

    public TokenBasedAuthentication(UserDetails principal) {
        super(principal.getAuthorities());
        this.principal = principal;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public UserDetails getPrincipal() {
        return principal;
    }

    public String getName() {
        return ((Account) principal).getId();
    }

    @Override
    public String toString() {
        return "TokenBasedAuthentication{" +
                "principal=" + principal +
                ", token='" + token + '\'' +
                '}';
    }
}

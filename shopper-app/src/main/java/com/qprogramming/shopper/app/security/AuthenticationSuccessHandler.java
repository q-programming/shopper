package com.qprogramming.shopper.app.security;

import com.qprogramming.shopper.app.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService _tokenService;

    @Autowired
    public AuthenticationSuccessHandler(TokenService tokenService) {
        this._tokenService = tokenService;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        clearAuthenticationAttributes(request);
        _tokenService.addTokenCookies(response, (Account) authentication.getPrincipal());
    }
}
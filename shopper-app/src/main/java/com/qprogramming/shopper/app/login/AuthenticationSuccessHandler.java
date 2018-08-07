package com.qprogramming.shopper.app.login;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.login.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jakub Romaniszyn on 19.07.2018.
 * <p>
 * Based on
 * https://github.com/bfwg/springboot-jwt-starter
 */
@Service
public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private TokenService tokenService;

    @Autowired
    public AuthenticationSuccessHandler(TokenService tokenService) {
        this.tokenService = tokenService;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        clearAuthenticationAttributes(request);
        Account account = (Account) authentication.getPrincipal();
        tokenService.createTokenCookies(response, account);
    }
}

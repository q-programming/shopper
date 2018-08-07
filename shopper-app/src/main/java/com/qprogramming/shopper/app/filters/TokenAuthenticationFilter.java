package com.qprogramming.shopper.app.filters;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.login.AnonAuthentication;
import com.qprogramming.shopper.app.login.token.TokenBasedAuthentication;
import com.qprogramming.shopper.app.login.token.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
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
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(TokenAuthenticationFilter.class);
    private AccountService accountService;
    private TokenService tokenService;

    public TokenAuthenticationFilter(AccountService accountService, TokenService tokenService) {
        this.accountService = accountService;
        this.tokenService = tokenService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!AuthUtils.isAuthenticated()) {
            String authToken = tokenService.getToken(request);
            if (authToken != null && !AuthUtils.skipPathRequest(request)) {
                // get username from token
                try {
                    String username = tokenService.getUsernameFromToken(authToken);
                    // get user
                    Account userDetails = accountService.loadUserByUsername(username);
                    // create authentication
                    TokenBasedAuthentication authentication = new TokenBasedAuthentication(userDetails);
                    authentication.setToken(authToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception e) {
                    SecurityContextHolder.getContext().setAuthentication(new AnonAuthentication());
                }
            } else {
                SecurityContextHolder.getContext().setAuthentication(new AnonAuthentication());
            }
        }
        chain.doFilter(request, response);
    }
}

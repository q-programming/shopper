package com.qprogramming.shopper.app.filters;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.login.AnonAuthentication;
import com.qprogramming.shopper.app.login.token.TokenBasedAuthentication;
import com.qprogramming.shopper.app.login.token.TokenService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Based on
 * https://github.com/bfwg/springboot-jwt-starter
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private AccountService accountService;
    private TokenService tokenService;

    public TokenAuthenticationFilter(AccountService accountService, TokenService tokenService) {
        this.accountService = accountService;
        this.tokenService = tokenService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (SecurityContextHolder.getContext().getAuthentication() == null || SecurityContextHolder.getContext().getAuthentication() instanceof AnonAuthentication) {
            String authToken = tokenService.getToken(request);
            if (authToken != null) {
                // get username from token
                String username = tokenService.getUsernameFromToken(authToken);
                if (username != null) {
                    Account account = accountService.findByUsername(username);
                    if (account != null) {
                        TokenBasedAuthentication authentication = new TokenBasedAuthentication(account);
                        authentication.setToken(authToken);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        chain.doFilter(request, response);
                        return;
                    }
                }
            }
            SecurityContextHolder.getContext().setAuthentication(new AnonAuthentication());
        }
        chain.doFilter(request, response);
    }
}

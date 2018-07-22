package com.qprogramming.shopper.app.filters;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.login.AnonAuthentication;
import com.qprogramming.shopper.app.login.token.TokenBasedAuthentication;
import com.qprogramming.shopper.app.login.token.TokenService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Jakub Romaniszyn on 19.07.2018.
 *
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
        String authToken = tokenService.getToken(request);
        if (authToken != null && !URLMatcher.skipPathRequest(request)) {
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
        chain.doFilter(request, response);
    }


    private boolean skipPathRequest(HttpServletRequest request, List<String> pathsToSkip) {
        Assert.notNull(pathsToSkip, "path cannot be null.");
        List<RequestMatcher> m = pathsToSkip.stream().map(AntPathRequestMatcher::new).collect(Collectors.toList());
        OrRequestMatcher matchers = new OrRequestMatcher(m);
        return matchers.matches(request);
    }
}

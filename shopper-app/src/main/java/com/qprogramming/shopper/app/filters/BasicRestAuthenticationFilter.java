package com.qprogramming.shopper.app.filters;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.login.AnonAuthentication;
import com.qprogramming.shopper.app.login.token.TokenService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 */
public class BasicRestAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(BasicRestAuthenticationFilter.class);
    public static final String AUTHORIZATION = "Authorization";
    public static final String AUTHENTICATION_SCHEME = "Basic";
    private AccountService accountService;
    private TokenService tokenService;


    public BasicRestAuthenticationFilter(AccountService accountService, TokenService tokenService) {
        this.accountService = accountService;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (!AuthUtils.isAuthenticated()) {//already logged in , skip
            String credentials = request.getHeader(AUTHORIZATION);
            if (StringUtils.isNotEmpty(credentials)
                    && !AuthUtils.skipPathRequest(request)) {
                final String encodedUserPassword = credentials.replaceFirst(AUTHENTICATION_SCHEME + " ", "");
                String usernameAndPassword = new String(Base64Utils.decode(encodedUserPassword.getBytes()));
                final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
                final String user = tokenizer.nextToken();
                final String password = tokenizer.nextToken();
                verifyAndLogin(user, password, response);
            } else {
                SecurityContextHolder.getContext().setAuthentication(new AnonAuthentication());
            }
        }
        chain.doFilter(request, response);
    }

    private void verifyAndLogin(String user, String passwordOrKey, HttpServletResponse response) {
        Account account = accountService.loadUserByUsername(user);
        //check if user tries to login with password or has devices and tries to login with it
        if (accountService.deviceAuth(passwordOrKey, account) || accountService.matches(passwordOrKey, account.getPassword())) {
            accountService.signin(account);
            tokenService.createTokenRESTCookies(response, account);
            LOG.debug("Authorization successful using base auth via rest. New token returned");
        } else {
            SecurityContextHolder.getContext().setAuthentication(new AnonAuthentication());
        }
    }
}

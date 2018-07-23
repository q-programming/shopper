package com.qprogramming.shopper.app.filters;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.login.AnonAuthentication;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Basic";
    private AccountService accountService;


    public BasicRestAuthenticationFilter(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
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
                verifyAndLogin(user, password);
            } else {
                SecurityContextHolder.getContext().setAuthentication(new AnonAuthentication());
            }
        }
        chain.doFilter(request, response);
    }

    private void verifyAndLogin(String user, String password) {
        Account account = accountService.loadUserByUsername(user);
        if (!accountService.matches(password, account.getPassword())) {
            SecurityContextHolder.getContext().setAuthentication(new AnonAuthentication());
        } else {
            accountService.signin(account);
        }
    }
}

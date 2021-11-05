package com.qprogramming.shopper.app.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jakub Romaniszyn on 2018-10-24
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LogoutHandlerImpl implements LogoutHandler {
    private final LogoutSuccess logoutSuccessHandler;

    @Override
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
        log.debug("logout(..): logging out the user : " + authentication.getName());
        try {
            this.logoutSuccessHandler.onLogoutSuccess(httpServletRequest, httpServletResponse, authentication);
        } catch (IOException e) {
            log.debug("There were errors while trying to logout {}", e.getMessage());
        }
    }
}

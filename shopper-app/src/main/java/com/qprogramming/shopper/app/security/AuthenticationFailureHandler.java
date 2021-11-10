package com.qprogramming.shopper.app.security;

import com.qprogramming.shopper.app.exceptions.AccountNotConfirmedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (exception.getCause() instanceof AccountNotConfirmedException) {
            response.sendError(HttpStatus.LOCKED.value(), "Account was not yet confirmed");
        } else {
            super.onAuthenticationFailure(request, response, exception);
        }
    }
}
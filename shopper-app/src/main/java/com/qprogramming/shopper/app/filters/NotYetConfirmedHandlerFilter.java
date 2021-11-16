package com.qprogramming.shopper.app.filters;

import com.qprogramming.shopper.app.exceptions.NotYetConfirmedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NotYetConfirmedHandlerFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (NotYetConfirmedException e) {
            response.setStatus(HttpStatus.LOCKED.value());
        }
    }
}

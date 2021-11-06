package com.qprogramming.shopper.app.security;

import com.qprogramming.shopper.app.account.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService _tokenService;
    private final AccountService _accountService;


    public TokenAuthenticationFilter(AccountService accountService, TokenService tokenService) {
        _tokenService = tokenService;
        _accountService = accountService;
    }


    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!AuthUtils.isAuthenticated()) {
            try {
                String jwt = _tokenService.getToken(request);
                if (StringUtils.hasText(jwt) && _tokenService.validateToken(jwt)) {
                    String userId = _tokenService.getUserIdFromToken(jwt);
                    UserDetails userDetails = _accountService.loadUserByUsername(userId);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    SecurityContextHolder.getContext().setAuthentication(new AnonAuthentication());
                }
            } catch (Exception ex) {
                SecurityContextHolder.getContext().setAuthentication(new AnonAuthentication());
                log.error("Could not set user authentication in security context", ex);
            }
        }
        filterChain.doFilter(request, response);
    }
}

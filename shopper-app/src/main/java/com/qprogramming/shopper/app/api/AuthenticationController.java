package com.qprogramming.shopper.app.api;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.login.RegisterForm;
import com.qprogramming.shopper.app.login.token.JwtAuthenticationRequest;
import com.qprogramming.shopper.app.login.token.TokenService;
import com.qprogramming.shopper.app.login.token.UserTokenState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 * <p>
 * Based on
 * https://github.com/bfwg/springboot-jwt-starter
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    private TokenService _tokenService;

    private AuthenticationManager _authenticationManager;

    private AccountService _accountService;

    @Value("${jwt.expires_in}")
    private int EXPIRES_IN;

    @Autowired
    public AuthenticationController(TokenService tokenService, AuthenticationManager authenticationManager, AccountService accountService) {
        this._tokenService = tokenService;
        this._authenticationManager = authenticationManager;
        _accountService = accountService;
    }

    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public ResponseEntity createAuthenticationToken(
            @RequestBody JwtAuthenticationRequest authenticationRequest, HttpServletResponse response) throws AuthenticationException, IOException {
        final Authentication authentication = _authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Account account = (Account) authentication.getPrincipal();
        _tokenService.createTokenCookies(response, account);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/auth/register", method = RequestMethod.POST)
    public ResponseEntity register(
            @RequestBody RegisterForm form) throws AuthenticationException {
        Optional<Account> byEmail = _accountService.findByEmail(form.getEmail());
        if (byEmail.isPresent()) {
            return ResponseEntity.badRequest().body("email");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("passwords");
        }
        if (form.getPassword().length() < 8) {
            return ResponseEntity.badRequest().body("weak");
        }
        Account account = form.createAccount();
        _accountService.createLocalAccount(account);
        //TODO send confirm mail
        return ResponseEntity.ok().build();
    }


    /**
     * Refreshes token (if it can be refreshed ) passed in request and returns back the token
     *
     * @param request  Request
     * @param response Response
     * @return refreshed token
     */
    @RequestMapping(value = "/api/refresh", method = RequestMethod.GET)
    public ResponseEntity<?> refreshAuthenticationToken(HttpServletRequest request, HttpServletResponse response) {

        String authToken = _tokenService.getToken(request);
        if (authToken != null && _tokenService.canTokenBeRefreshed(authToken)) {
            // TODO check user password last update
            String refreshedToken = _tokenService.refreshToken(authToken);
            _tokenService.refreshCookie(refreshedToken, response);
            UserTokenState userTokenState = new UserTokenState(refreshedToken, EXPIRES_IN);
            return ResponseEntity.ok(userTokenState);
        } else {
            UserTokenState userTokenState = new UserTokenState();
            return ResponseEntity.accepted().body(userTokenState);
        }
    }
}

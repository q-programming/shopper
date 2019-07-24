package com.qprogramming.shopper.app.api;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.account.PasswordForm;
import com.qprogramming.shopper.app.account.devices.NewDevice;
import com.qprogramming.shopper.app.account.event.AccountEvent;
import com.qprogramming.shopper.app.account.event.AccountEventType;
import com.qprogramming.shopper.app.config.mail.Mail;
import com.qprogramming.shopper.app.config.mail.MailService;
import com.qprogramming.shopper.app.login.RegisterForm;
import com.qprogramming.shopper.app.login.token.JwtAuthenticationRequest;
import com.qprogramming.shopper.app.login.token.TokenService;
import com.qprogramming.shopper.app.login.token.UserTokenState;
import com.qprogramming.shopper.app.support.Utils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 * <p>
 * Based on
 * https://github.com/bfwg/springboot-jwt-starter
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);

    private TokenService _tokenService;
    private AuthenticationManager _authenticationManager;
    private AccountService _accountService;

    @Value("${jwt.expires_in}")
    private int EXPIRES_IN;

    @Autowired
    public AuthenticationController(TokenService tokenService, AuthenticationManager authenticationManager, AccountService accountService) {
        this._tokenService = tokenService;
        this._authenticationManager = authenticationManager;
        this._accountService = accountService;
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
    @Transactional
    public ResponseEntity register(
            @RequestBody RegisterForm form) throws AuthenticationException {
        Optional<Account> byEmail = _accountService.findByEmail(form.getEmail());
        if (byEmail.isPresent()) {
            return ResponseEntity.status(CONFLICT).body("email");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            return ResponseEntity.status(CONFLICT).body("passwords");
        }
        if (form.getPassword().length() < 8) {
            return ResponseEntity.status(CONFLICT).body("weak");
        }
        try {
            Account account = form.createAccount();
            account = _accountService.createLocalAccount(account);
            AccountEvent event = _accountService.createConfirmEvent(account);
            _accountService.sendConfirmEmail(account, event);
        } catch (MessagingException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("mailing");
        }
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/auth/new-device", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity registerNewDevice(
            @RequestBody String email) throws AuthenticationException {
        Optional<Account> byEmail = _accountService.findByEmail(email);
        if (!byEmail.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        try {
            Account account = byEmail.get();
            NewDevice newDevice = _accountService.registerNewDevice(account);
            AccountEvent event = _accountService.createConfirmDeviceEvent(account, newDevice.getId());
            _accountService.sendConfirmEmail(account, event);
            return ResponseEntity.ok(newDevice);
        } catch (MessagingException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("mailing");
        }
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


    @RequestMapping(value = "/auth/confirm", method = RequestMethod.POST)
    public ResponseEntity confirmOperation(@RequestBody() String token) {
        UUID uuid = UUID.fromString(token);
        Optional<AccountEvent> eventOptional = _accountService.findEvent(token);
        if (!eventOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        AccountEvent event = eventOptional.get();
        DateTime date = new DateTime(Utils.getTimeFromUUID(uuid));
        DateTime expireDate = date.plusHours(24);
        if (date.isAfter(expireDate)) {
            _accountService.removeEvent(event);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("expired");
        }
        if (!event.getType().equals(AccountEventType.ACCOUNT_CONFIRM) && !event.getAccount().equals(Utils.getCurrentAccount())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        HashMap<String, String> model = new HashMap<>();
        switch (event.getType()) {
            case ACCOUNT_CONFIRM:
                _accountService.confirm(event.getAccount());
                model.put("result", "confirmed");
                break;
            case PASSWORD_RESET:
                if (new DateTime().isAfter(date.plusHours(12))) {
                    _accountService.removeEvent(event);
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("expired");
                }
                break;
        }
        return ResponseEntity.ok(model);

    }

    @Transactional
    @RequestMapping(value = "/auth/password-reset", method = RequestMethod.POST)
    public ResponseEntity<?> passwordReset(@RequestBody String email) {
        Optional<Account> optionalAccount = _accountService.findByEmail(email);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            AccountEvent event = _accountService.createPasswordResetEvent(account);
            try {
                _accountService.sendConfirmEmail(account, event);
            } catch (MessagingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        return ResponseEntity.ok().build();
    }

    @Transactional
    @RequestMapping(value = "/auth/password-change", method = RequestMethod.POST)
    public ResponseEntity<?> changePassword(@RequestBody PasswordForm form) {
        UUID uuid = UUID.fromString(form.getToken());
        Optional<AccountEvent> eventOptional = _accountService.findEvent(form.getToken());
        if (!eventOptional.isPresent()) {
            return ResponseEntity.status(NOT_FOUND).build();
        }
        AccountEvent event = eventOptional.get();
        DateTime date = new DateTime(Utils.getTimeFromUUID(uuid));
        if (new DateTime().isAfter(date.plusHours(12))) {
            _accountService.removeEvent(event);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("expired");
        }
        Account account = event.getAccount();
        account.setPassword(form.getPassword());
        _accountService.encodePassword(account);
        _accountService.update(account);
        _accountService.eventConfirmed(event);
        HashMap<String, String> model = new HashMap<>();
        model.put("result", "changed");
        return ResponseEntity.ok(model);
    }
}

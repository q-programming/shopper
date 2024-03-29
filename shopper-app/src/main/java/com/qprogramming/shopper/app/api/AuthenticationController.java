package com.qprogramming.shopper.app.api;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.account.PasswordForm;
import com.qprogramming.shopper.app.account.devices.NewDevice;
import com.qprogramming.shopper.app.account.event.AccountEvent;
import com.qprogramming.shopper.app.account.event.AccountEventType;
import com.qprogramming.shopper.app.exceptions.DeviceNotFoundException;
import com.qprogramming.shopper.app.login.RegisterForm;
import com.qprogramming.shopper.app.login.token.UserTokenState;
import com.qprogramming.shopper.app.security.TokenService;
import com.qprogramming.shopper.app.support.Utils;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
@RequiredArgsConstructor
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {


    private final TokenService _tokenService;
    private final AuthenticationManager _authenticationManager;
    private final AccountService _accountService;

    @Value("${jwt.expires_in}")
    private int EXPIRES_IN;

    @RequestMapping(value = "/auth/register", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<?> register(
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
    public ResponseEntity<?> registerNewDevice(
            @RequestBody RegisterForm form) throws AuthenticationException {
        Optional<Account> byEmail = _accountService.findByEmail(form.getEmail());
        if (byEmail.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            Account account = byEmail.get();
            NewDevice newDevice = _accountService.registerNewDevice(account, form.getName());
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

    @Transactional
    @RequestMapping(value = "/auth/confirm", method = RequestMethod.POST)
    public ResponseEntity<?> confirmOperation(@RequestBody() String token) {
        UUID uuid = UUID.fromString(token);
        Optional<AccountEvent> eventOptional = _accountService.findEvent(token);
        if (eventOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        AccountEvent event = eventOptional.get();
        DateTime date = new DateTime(Utils.getTimeFromUUID(uuid));
        DateTime expireDate = date.plusHours(24);
        if (new DateTime().isAfter(expireDate)) {
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
            case DEVICE_CONFIRM:
                try {
                    _accountService.confirmDevice(event.getAccount(), event.getData());
                    model.put("result", "device_confirmed");
                } catch (DeviceNotFoundException e) {
                    return ResponseEntity.status(NOT_FOUND).build();
                }
                break;
            case PASSWORD_RESET:
                if (new DateTime().isAfter(date.plusHours(12))) {
                    _accountService.removeEvent(event);
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("expired");
                }
                break;
        }
        _accountService.removeEvent(event);
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
        if (eventOptional.isEmpty()) {
            return ResponseEntity.status(NOT_FOUND).build();
        }
        AccountEvent event = eventOptional.get();
        DateTime date = new DateTime(Utils.getTimeFromUUID(uuid));
        if (new DateTime().isAfter(date.plusHours(12))) {
            _accountService.removeEvent(event);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("expired");
        }
        if (!form.getPassword().equals(form.getConfirmpassword())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("mismatch");
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

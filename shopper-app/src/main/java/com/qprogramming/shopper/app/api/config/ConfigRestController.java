package com.qprogramming.shopper.app.api.config;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.config.mail.MailService;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.settings.Settings;
import com.qprogramming.shopper.app.support.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.qprogramming.shopper.app.settings.Settings.*;

/**
 * Created by Jakub Romaniszyn on 2018-09-13
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/config")
public class ConfigRestController {

    private final MailService mailService;
    private final PropertyService propertyService;


    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    public ResponseEntity<Settings> applicationSettings(HttpServletRequest request) {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Settings settings = new Settings();
        settings.setLanguage(propertyService.getDefaultLang());
        Settings.Email emailSettings = new Settings.Email();
        emailSettings.setHost(propertyService.getProperty(APP_EMAIL_HOST));
        try {
            emailSettings.setPort(Integer.parseInt(propertyService.getProperty(APP_EMAIL_PORT)));
        } catch (NumberFormatException e) {
            log.warn("Failed to set port from properties");
        }
        emailSettings.setUsername(propertyService.getProperty(APP_EMAIL_USERNAME));
        emailSettings.setPassword(propertyService.getProperty(APP_EMAIL_PASS));
        emailSettings.setEncoding(propertyService.getProperty(APP_EMAIL_ENCODING));
        emailSettings.setFrom(propertyService.getProperty(APP_EMAIL_FROM));
        settings.setEmail(emailSettings);
        settings.setAppUrl(propertyService.getProperty(APP_URL));
        if (StringUtils.isEmpty(settings.getAppUrl())) {//app url was not set, get default request
            settings.setAppUrl(setDefaultAPPURL(request));
        }
        return ResponseEntity.ok(settings);
    }

    private String setDefaultAPPURL(HttpServletRequest request) {
        return (String) propertyService.update(APP_URL, Utils.getFullPathFromRequest(request)).getValue();
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/settings/email", method = RequestMethod.POST)
    public ResponseEntity changeEmailSettings(@RequestBody Settings appSettings) {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Email settings = appSettings.getEmail();
        try {
            mailService.testConnection(settings.getHost(), settings.getPort(), settings.getUsername(), settings.getPassword());
        } catch (MessagingException e) {
            log.warn("Bad SMTP configuration: {}", e);
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
        propertyService.update(APP_EMAIL_HOST, settings.getHost());
        propertyService.update(APP_EMAIL_PORT, String.valueOf(settings.getPort()));
        propertyService.update(APP_EMAIL_USERNAME, settings.getUsername());
        propertyService.update(APP_EMAIL_PASS, settings.getPassword());
        propertyService.update(APP_EMAIL_ENCODING, settings.getEncoding());
        propertyService.update(APP_EMAIL_FROM, settings.getFrom());
        mailService.initMailSender();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RolesAllowed("ROLE_ADMIN")
    @RequestMapping(value = "/settings/app", method = RequestMethod.POST)
    public ResponseEntity<?> changeAppSettings(@RequestBody Settings settings) {
        Account currentAccount = Utils.getCurrentAccount();
        if (currentAccount == null || !currentAccount.getIsAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        propertyService.update(APP_DEFAULT_LANG, settings.getLanguage());
        propertyService.update(APP_URL, settings.getAppUrl());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RolesAllowed("ROLE_USER")
    @RequestMapping(value = "/categories/defaults", method = RequestMethod.GET)
    public ResponseEntity getDefaultSorting() {
        return ResponseEntity.ok(Collections.singleton(propertyService.getProperty(APP_CATEGORY_ORDER)));
    }

    @RequestMapping(value = "/default-language", method = RequestMethod.GET)
    public ResponseEntity getDefaultLanguage() {
        Map<String, Object> model = new HashMap<>();
        model.put("language", propertyService.getDefaultLang());
        return ResponseEntity.ok(model);
    }
}

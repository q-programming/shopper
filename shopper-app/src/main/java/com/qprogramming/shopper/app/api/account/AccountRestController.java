package com.qprogramming.shopper.app.api.account;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.account.DisplayAccount;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.support.Utils;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account")
public class AccountRestController {
    private static final Logger LOG = LoggerFactory.getLogger(AccountRestController.class);
    private AccountService accountService;

    @Autowired
    public AccountRestController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Returns currently logged in user as {@link Account}
     *
     * @return currently logged in user
     */
    @RequestMapping("/whoami")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Account user() {
        return (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * Returns all application users
     *
     * @return List of {@link Account}
     */
    @RequestMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<Account> allUsers() {
        return accountService.findAll();
    }

    /**
     * Returns bytes with Account avatar image
     *
     * @param id Account id for which avatar will be returned
     * @return {@link com.qprogramming.shopper.app.account.avatar.Avatar} account avatar
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/{id}/avatar", method = RequestMethod.GET)
    public ResponseEntity<?> userAvatar(@PathVariable(value = "id") String id) {
        try {
            Account account = accountService.findById(id);
            return ResponseEntity.ok(accountService.getAccountAvatar(account));
        } catch (AccountNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Translates ids into name surname combination
     *
     * @param ids Account ids to be translated into Name Surname
     * @return Set of {@link DisplayAccount}
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public ResponseEntity<Set<DisplayAccount>> getUsers(@RequestBody String[] ids) {
        Set<DisplayAccount> displayAccounts = Arrays.stream(ids).map(s -> {
            try {
                return new DisplayAccount(accountService.findById(s));
            } catch (AccountNotFoundException e) {
                LOG.error("Account with id {} was not found");
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        return ResponseEntity.ok(displayAccounts);
    }

    /**
     * Upload new avatar for currently logged in user
     *
     * @param avatarStream base64 based avatar stream
     * @return {@link HttpStatus#OK} if upload was successful
     */
    @Transactional
    @RequestMapping(value = "/avatar-upload", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> uploadNewAvatar(@RequestBody String avatarStream) {
        Account account = Utils.getCurrentAccount();
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] data = Base64.decodeBase64(avatarStream);
        accountService.updateAvatar(account, data);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Change settings for currently logged in user
     *
     * @param lang new language
     * @return {@link HttpStatus#OK} if upload was successful
     */
    @Transactional
    @RequestMapping(value = "/settings/language", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> changeLanguage(@RequestBody String lang) {
        Account currentAccount = Utils.getCurrentAccount();
        currentAccount.setLanguage(lang);
        accountService.update(currentAccount);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}

package com.qprogramming.shopper.app.account;


import com.qprogramming.shopper.app.account.authority.Authority;
import com.qprogramming.shopper.app.account.authority.AuthorityService;
import com.qprogramming.shopper.app.account.authority.Role;
import com.qprogramming.shopper.app.config.property.PropertyService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 */
@Service
public class AccountService implements UserDetailsService {

    private static final String API_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";

    private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);
    private PropertyService propertyService;
    private AccountRepository accountRepository;
    private AuthorityService authorityService;
    private AccountPasswordEncoder accountPasswordEncoder;

    @Autowired
    public AccountService(PropertyService propertyService, AccountRepository accountRepository, AuthorityService authorityService, AccountPasswordEncoder accountPasswordEncoder) {
        this.propertyService = propertyService;
        this.accountRepository = accountRepository;
        this.authorityService = authorityService;
        this.accountPasswordEncoder = accountPasswordEncoder;
    }

    public void signin(Account account) {
        SecurityContextHolder.getContext().setAuthentication(authenticate(account));
    }

    private Authentication authenticate(Account account) {
        return new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities());
    }

    public Account findById(String id) {
        return accountRepository.findOneById(id);
    }

    public Account createOAuthAcount(Account account) {
        List<Authority> auths = new ArrayList<>();
        Authority role = authorityService.findByRole(Role.ROLE_USER);
        auths.add(role);
        if (accountRepository.findAll().size() == 0) {
            Authority admin = authorityService.findByRole(Role.ROLE_ADMIN);
            auths.add(admin);
        }
        account.setAuthorities(auths);
        if (StringUtils.isEmpty(account.getLanguage())) {
            setDefaultLocale(account);
        }
        //generate api key
        generatePassword(account);
        return accountRepository.save(account);
    }

    private void generatePassword(Account account) {
        char[] possibleCharacters = API_CHARS.toCharArray();
        String password = RandomStringUtils.random(32, 0, possibleCharacters.length - 1, false, false, possibleCharacters, new SecureRandom());
        //TODO remove afterwards
        LOG.info("******Generated new password for " + account.getEmail() + ". Password is : \"" + password + "\" ******");
        account.setPassword(encode(password));
    }

    public String generateID() {
        String uuid = UUID.randomUUID().toString();
        while (accountRepository.findOneById(uuid) != null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    private void setDefaultLocale(Account account) {
        String defaultLanguage = propertyService.getDefaultLang();
        account.setLanguage(defaultLanguage);
    }

    public Account findByEmail(String email) {
        return accountRepository.findOneByEmail(email);
    }

    @Override
    public Account loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findOneByEmail(username);
        if (account == null) {
            account = accountRepository.findOneByUsername(username);
            if (account == null) {
                throw new UsernameNotFoundException("user not found");
            }
        }
        //TODO remove later on
        if (StringUtils.isEmpty(account.getPassword())) {
            generatePassword(account);
            account = accountRepository.save(account);
        }
        return account;
    }

    public Account findByUsername(String username) {
        return accountRepository.findOneByUsername(username);
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public String encode(String string) {
        return accountPasswordEncoder.encode(string);
    }

    public boolean matches(String raw, String encoded) {
        return accountPasswordEncoder.matches(raw, encoded);
    }
}

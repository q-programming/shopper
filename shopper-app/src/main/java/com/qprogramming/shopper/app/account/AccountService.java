package com.qprogramming.shopper.app.account;


import com.qprogramming.shopper.app.config.property.PropertyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class AccountService implements UserDetailsService {

    private PropertyService propertyService;
    private AccountRepository accountRepository;

    @Autowired
    public AccountService(PropertyService propertyService, AccountRepository accountRepository) {
        this.propertyService = propertyService;
        this.accountRepository = accountRepository;
    }

    public void signin(Account account) {
        SecurityContextHolder.getContext().setAuthentication(authenticate(account));
    }

    private Authentication authenticate(Account account) {
        return new UsernamePasswordAuthenticationToken(account, null, Collections.singleton(createAuthority(account)));
    }

    private GrantedAuthority createAuthority(Account account) {
        return new SimpleGrantedAuthority(account.getRole().toString());
    }

    public Account findById(String id) {
        return accountRepository.findOneById(id);
    }

    public Account createOAuthAcount(Account account) {
        if (accountRepository.findAll().size() == 0) {
            account.setRole(Roles.ROLE_ADMIN);
        } else {
            account.setRole(Roles.ROLE_USER);
        }
        if (StringUtils.isEmpty(account.getLanguage())) {
            setDefaultLocale(account);
        }
        return accountRepository.save(account);
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findOneByEmail(username);
        if (account == null) {
            account = accountRepository.findOneByUsername(username);
            if (account == null) {
                throw new UsernameNotFoundException("user not found");
            }
        }
        account.setAuthority(account.getRole());
        return account;

    }

    public Account findByUsername(String username) {
        return accountRepository.findOneByUsername(username);
    }
}

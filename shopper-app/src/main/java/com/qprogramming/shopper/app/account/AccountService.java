package com.qprogramming.shopper.app.account;


import com.qprogramming.shopper.app.account.authority.Authority;
import com.qprogramming.shopper.app.account.authority.AuthorityService;
import com.qprogramming.shopper.app.account.authority.Role;
import com.qprogramming.shopper.app.config.property.PropertyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService implements UserDetailsService {

    private PropertyService propertyService;
    private AccountRepository accountRepository;
    private AuthorityService authorityService;

    @Autowired
    public AccountService(PropertyService propertyService, AccountRepository accountRepository, AuthorityService authorityService) {
        this.propertyService = propertyService;
        this.accountRepository = accountRepository;
        this.authorityService = authorityService;
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
    public Account loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findOneByEmail(username);
        if (account == null) {
            account = accountRepository.findOneByUsername(username);
            if (account == null) {
                throw new UsernameNotFoundException("user not found");
            }
        }
        return account;
    }

    public Account findByUsername(String username) {
        return accountRepository.findOneByUsername(username);
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }
}

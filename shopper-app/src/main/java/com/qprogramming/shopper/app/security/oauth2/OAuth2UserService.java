package com.qprogramming.shopper.app.security.oauth2;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.account.AccountType;
import com.qprogramming.shopper.app.exceptions.OAuth2AuthenticationProcessingException;
import com.qprogramming.shopper.app.security.oauth2.user.OAuth2UserInfo;
import com.qprogramming.shopper.app.security.oauth2.user.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.net.MalformedURLException;
import java.util.Optional;

/**
 * Service to handle OAuthUsers logging and registering
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final AccountService accountService;

    /**
     * Loads OAuth user from request and processes it
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    protected OAuth2User loadUserData(OAuth2UserRequest oAuth2UserRequest) {
        return super.loadUser(oAuth2UserRequest);
    }

    /**
     * Process OAuth user.
     * If user already has account in application , return it. Otherwise grab all data from attributes, fetch avatar and then return freshly saved Account
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if (ObjectUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }
        Optional<Account> optionalAccount = accountService.findByEmail(oAuth2UserInfo.getEmail());
        return optionalAccount.orElseGet(() -> registerNewUser(oAuth2UserRequest, oAuth2UserInfo));
    }

    private Account registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        Account account;
        account = new Account();
        account.setId(accountService.generateID());
        account.setType(AccountType.type(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        account.setName(oAuth2UserInfo.getFirstName());
        account.setSurname(oAuth2UserInfo.getSurname());
        account.setEmail(oAuth2UserInfo.getEmail());
        account.setEnabled(true);
        accountService.setLocale(account);
        setUsername(account);
        account = accountService.createAccount(account);
        try {
            accountService.createAvatar(account, oAuth2UserInfo.getImageUrl());
        } catch (MalformedURLException e) {
            log.error("Failed to get avatar from google account: {}", e.getMessage());
        }
        return account;
    }

    private void setUsername(Account account) {
        String userString = account.getEmail().split("@")[0];
        StringBuilder username = new StringBuilder(userString.replace(".", "_"));
        val optionalAccount = accountService.findByUsername(username.toString());
        if (optionalAccount.isPresent()) {
            username.append("_").append(account.getType().getCode());
        }
        account.setUsername(username.toString());
    }


}

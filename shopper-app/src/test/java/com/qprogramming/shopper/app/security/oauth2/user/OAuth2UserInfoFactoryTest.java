package com.qprogramming.shopper.app.security.oauth2.user;

import com.qprogramming.shopper.app.account.AccountType;
import com.qprogramming.shopper.app.exceptions.OAuth2AuthenticationProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2UserInfoFactoryTest {
    private final HashMap<String, Object> attribs = new HashMap<>() {
        {
            put("name", "John");
            put("email", "john@doe.com");
        }
    };


    @Test
    void createGoogleTest() {
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(AccountType.GOOGLE.toString(), attribs);
        assertThat(userInfo instanceof GoogleOAuth2UserInfo).isTrue();
    }

    @Test
    void createFacebookTest() {
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(AccountType.FACEBOOK.toString(), attribs);
        assertThat(userInfo instanceof FacebookOAuth2UserInfo).isTrue();
    }

    @Test
    void createNotSupportedTest() {
        Assertions.assertThrows(
                OAuth2AuthenticationProcessingException.class,
                () -> OAuth2UserInfoFactory.getOAuth2UserInfo(AccountType.LOCAL.toString(), attribs));
    }

    @Test
    void createWrongEnumTest() {
        Assertions.assertThrows(
                OAuth2AuthenticationProcessingException.class,
                () -> OAuth2UserInfoFactory.getOAuth2UserInfo(AccountType.LOCAL.getCode(), attribs));
    }
}
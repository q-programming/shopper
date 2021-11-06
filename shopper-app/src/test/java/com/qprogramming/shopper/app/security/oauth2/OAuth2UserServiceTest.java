package com.qprogramming.shopper.app.security.oauth2;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.account.AccountType;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

class OAuth2UserServiceTest extends MockedAccountTestBase {

    private final ClientRegistration registration = TestClientRegistrations.clientRegistration().registrationId(AccountType.GOOGLE.toString()).build();
    private OAuth2UserService oAuth2UserService;
    @Mock
    private AccountService accountServiceMock;

    @Mock
    private OAuth2AccessToken accessTokenMock;

    @BeforeEach
    public void setUp() {
        oAuth2UserService = spy(new OAuth2UserService(accountServiceMock));
    }

    @Test
    void loadUserCreateNewTest() {
        String randomId = "MY-NEW-RANDOM";
        val request = new OAuth2UserRequest(registration, accessTokenMock, new HashMap<>());
        //fill attributes
        val attributes = new HashMap<String, Object>();
        attributes.put("email", testAccount.getEmail());
        attributes.put("name", testAccount.getName());
        testAccount.setAttributes(attributes);
        when(accountServiceMock.findByEmail(testAccount.getEmail())).thenReturn(Optional.empty());
        when(accountServiceMock.createAccount(any(Account.class))).then(returnsFirstArg());
        when(accountServiceMock.generateID()).thenReturn(randomId);
        doReturn(testAccount).when(oAuth2UserService).loadUserData(any());
        val result = oAuth2UserService.loadUser(request);
        assertThat(result).isInstanceOf(Account.class);
        assertThat(((Account) result).getType()).isEqualTo(AccountType.GOOGLE);
        assertThat(((Account) result).getId()).isEqualTo(randomId);
    }

    @Test
    void loadUserNoEmailFoundTest() {
        val request = new OAuth2UserRequest(registration, accessTokenMock, new HashMap<>());
        when(accountServiceMock.findByEmail(testAccount.getEmail())).thenReturn(Optional.empty());
        doReturn(testAccount).when(oAuth2UserService).loadUserData(any());
        assertThrows(InternalAuthenticationServiceException.class, () -> oAuth2UserService.loadUser(request));
    }

    @Test
    void loadUserTest() {
        val request = new OAuth2UserRequest(registration, accessTokenMock, new HashMap<>());
        //fill attributes
        val attributes = new HashMap<String, Object>();
        attributes.put("email", testAccount.getEmail());
        testAccount.setAttributes(attributes);
        when(accountServiceMock.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));
        doReturn(testAccount).when(oAuth2UserService).loadUserData(any());
        val result = oAuth2UserService.loadUser(request);
        assertThat(result).isInstanceOf(Account.class);
        assertThat(((Account) result).getEmail()).isEqualTo(testAccount.getEmail());
    }
}
package com.qprogramming.shopper.app.login.token;

import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.Account;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenBasedAuthenticationTest {

    Account testAccount;

    @BeforeEach
    void setup() {
        testAccount = TestUtil.createAccount();
        testAccount.setAuthorities(Collections.emptyList());
    }

    @Test
    public void isAuthenticated() {
        val auth = new TokenBasedAuthentication(testAccount);
        assertThat(auth.isAuthenticated()).isTrue();
    }

    @Test
    public void getCredentials() {
        val auth = new TokenBasedAuthentication(testAccount);
        String token = "token";
        auth.setToken(token);
        assertThat(auth.getCredentials()).isEqualTo(token);
    }

    @Test
    public void getPrincipal() {
        val auth = new TokenBasedAuthentication(testAccount);
        assertThat(auth.getPrincipal()).isEqualTo(testAccount);
    }

    @Test
    public void getName() {
        val auth = new TokenBasedAuthentication(testAccount);
        assertThat(auth.getName()).isEqualTo(testAccount.getId());

    }
}
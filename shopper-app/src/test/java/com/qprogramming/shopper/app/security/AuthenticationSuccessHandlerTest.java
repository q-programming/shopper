package com.qprogramming.shopper.app.security;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.login.token.TokenBasedAuthentication;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AuthenticationSuccessHandlerTest extends MockedAccountTestBase {

    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private HttpServletRequest requestMock;
    @Mock
    private TokenService tokenService;

    private AuthenticationSuccessHandler successHandler;

    @BeforeEach
    public void setUp() {
        successHandler = new AuthenticationSuccessHandler(tokenService);
        testAccount = TestUtil.createAccount();
    }


    @Test
    public void onAuthenticationSuccessTest() {
        val auth = new TokenBasedAuthentication(testAccount);
        successHandler.onAuthenticationSuccess(requestMock, responseMock, auth);
        verify(tokenService, times(1)).addTokenCookies(responseMock, testAccount);
    }
}
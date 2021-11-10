package com.qprogramming.shopper.app.security;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.exceptions.AccountNotConfirmedException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthenticationFailureHandlerTest extends MockedAccountTestBase {

    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private HttpServletRequest requestMock;
    private AuthenticationFailureHandler authFailureHandler;

    @BeforeEach
    public void setUp() {
        authFailureHandler = new AuthenticationFailureHandler();
        testAccount = TestUtil.createAccount();
    }


    @Test
    public void onAuthenticationFailureAccountNotConfirmedTest() throws Exception {
        val exceptionMock = mock(AuthenticationException.class);
        when(exceptionMock.getCause()).thenReturn(new AccountNotConfirmedException("not confirmed"));
        authFailureHandler.onAuthenticationFailure(requestMock, responseMock, exceptionMock);
        verify(responseMock, times(1)).sendError(anyInt(), anyString());
    }

    @Test
    public void onAuthenticationFailureTest() throws Exception {
        val exceptionMock = mock(AuthenticationException.class);
        when(exceptionMock.getCause()).thenReturn(new Throwable("Wrong password"));
        authFailureHandler.onAuthenticationFailure(requestMock, responseMock, exceptionMock);
        verify(responseMock, times(1)).sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());

    }
}
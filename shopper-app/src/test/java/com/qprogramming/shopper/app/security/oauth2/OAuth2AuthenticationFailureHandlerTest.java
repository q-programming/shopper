package com.qprogramming.shopper.app.security.oauth2;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OAuth2AuthenticationFailureHandlerTest extends MockedAccountTestBase {

    @Mock
    private TokenService tokenServiceMock;
    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository repositoryMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private HttpServletRequest requestMock;


    private OAuth2AuthenticationFailureHandler authFailureHandler;


    @BeforeEach
    public void setUp() {
        authFailureHandler = new OAuth2AuthenticationFailureHandler(tokenServiceMock, repositoryMock);
        testAccount = TestUtil.createAccount();
    }


    @Test
    public void onAuthenticationFailure() throws Exception {
        when(requestMock.getContextPath()).thenReturn("/");
        when(tokenServiceMock.getCookie(any(HttpServletRequest.class), anyString())).thenReturn(Optional.of(new Cookie("name", "value")));
        when(responseMock.encodeRedirectURL(anyString())).then(returnsFirstArg());
        authFailureHandler.onAuthenticationFailure(requestMock, responseMock, new TestException("failed"));
        String result = "/value?error=failed";
        verify(responseMock, times(1)).sendRedirect(result);
    }


    static class TestException extends AuthenticationException {
        public TestException(String msg) {
            super(msg);
        }
    }
}
package com.qprogramming.shopper.app.security.oauth2;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.qprogramming.shopper.app.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME;
import static com.qprogramming.shopper.app.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class HttpCookieOAuth2AuthorizationRequestRepositoryTest extends MockedAccountTestBase {

    @Mock
    private TokenService tokenService;
    private HttpCookieOAuth2AuthorizationRequestRepository cookieReqRepo;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private HttpServletRequest requestMock;

    @BeforeEach
    void setUp() {
        setup();
        cookieReqRepo = new HttpCookieOAuth2AuthorizationRequestRepository(tokenService);
    }

    @Test
    void loadAuthorizationRequestTest() {
        cookieReqRepo.loadAuthorizationRequest(requestMock);
        verify(tokenService, times(1)).getDeserializedCookie(requestMock, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }

    @Test
    void saveAuthorizationRequestNullAuthTest() {
        cookieReqRepo.saveAuthorizationRequest(null, requestMock, responseMock);
        verify(tokenService, times(1)).deleteCookie(requestMock, responseMock, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        verify(tokenService, times(1)).deleteCookie(requestMock, responseMock, REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}
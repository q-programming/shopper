package com.qprogramming.shopper.app.login.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.support.TimeProvider;
import org.assertj.core.util.DateUtil;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by Jakub Romaniszyn on 2018-07-23
 */
@ActiveProfiles("test")
public class TokenServiceTest {

    @Mock
    private AccountService accountService;
    private ObjectMapper objectMapper;
    @Mock
    private TimeProvider timeProvider;

    private TokenService tokenService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        objectMapper = spy(ObjectMapper.class);
        tokenService = new TokenService(objectMapper, accountService, timeProvider);
        ReflectionTestUtils.setField(tokenService, "EXPIRES_IN", 10); // 10 sec
        ReflectionTestUtils.setField(tokenService, "SECRET", "mySecret");

    }

    @Test
    public void getUsernameFromToken() {
        when(timeProvider.getCurrentTimeMillis())
                .thenReturn(new DateTime().getMillis());
        String token = createToken();
        assertThat(tokenService.getUsernameFromToken(token)).isEqualTo(TestUtil.EMAIL);
    }

    @Test
    public void createTokenCookies() {
    }

    @Test
    public void getToken() {
    }

    @Test
    public void tokensGeneratedOnDifferentDatesDifferent() {
        when(timeProvider.getCurrentTimeMillis())
                .thenReturn(new DateTime().minusDays(1).getMillis())
                .thenReturn(new DateTime().getMillis());
        String token = createToken();
        String token2 = createToken();
        assertThat(token).isNotEqualTo(token2);
    }

    @Test
    public void getCreatedDateFromToken() {
        DateTime now = new DateTime();
        when(timeProvider.getCurrentTimeMillis())
                .thenReturn(now.getMillis());
        final String token = createToken();
        assertThat(tokenService.getIssuedAtDateFromToken(token)).isInSameMinuteWindowAs(now.toDate());
    }

    @Test
    public void canTokenBeRefreshedFail() {
        when(timeProvider.getCurrentTimeMillis())
                .thenReturn(new DateTime().minusDays(1).getMillis());
        String token = createToken();
        assertThat(tokenService.canTokenBeRefreshed(token)).isFalse();
    }

    @Test
    public void canTokenBeRefreshed() {
        when(timeProvider.getCurrentTimeMillis())
                .thenReturn(new DateTime().getMillis());
        String token = createToken();
        assertThat(tokenService.canTokenBeRefreshed(token)).isTrue();
    }

    @Test
    public void refreshToken() {
        final Date now = DateUtil.now();
        when(timeProvider.getCurrentTimeMillis())
                .thenReturn(new DateTime().minusSeconds(5).getMillis())
                .thenReturn(new DateTime().minusSeconds(5).getMillis())
                .thenReturn(new DateTime().getMillis());
        String token = createToken();
        String refreshedToken = tokenService.refreshToken(token);
        assertThat(tokenService.getIssuedAtDateFromToken(refreshedToken)).isInSameMinuteWindowAs(now);
    }

    private String createToken() {
        return tokenService.generateToken(TestUtil.EMAIL);
    }


}
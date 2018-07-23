package com.qprogramming.shopper.app.api;

import com.qprogramming.shopper.app.RestControllerTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.login.token.TokenService;
import com.qprogramming.shopper.app.support.DummyHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;

import javax.servlet.http.Cookie;
import java.io.PrintWriter;
import java.util.Set;

import static com.qprogramming.shopper.app.filters.BasicRestAuthenticationFilter.AUTHENTICATION_SCHEME;
import static com.qprogramming.shopper.app.filters.BasicRestAuthenticationFilter.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthenticationControllerTest extends RestControllerTestBase {

    private Account testAccount;

    @Mock
    private PrintWriter writerMock;

    @Autowired
    private TokenService tokenService;

    @Before
    @Override
    public void setup() {
        super.setup();
        MockitoAnnotations.initMocks(this);

    }

    @Test
    @WithAnonymousUser
    public void shouldGetUnauthorizedWithoutRole() throws Exception {
        this.mvc.perform(get("/api/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = TestUtil.EMAIL, userDetailsServiceBeanName = "accountService")
    public void getPersonsSuccessfullyWithUserRole() throws Exception {
        this.mvc.perform(get("/api/account/whoami"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAllUserSuccessWithAdminRole() throws Exception {
        this.mvc.perform(get("/api/account/all"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser
    public void getAllUserFailWithUserRole() throws Exception {
        this.mvc.perform(get("/api/account/all"))
                .andExpect(status().is4xxClientError());
    }

    //TODO check why it's not working in run all tests
//    @Test
//    public void refreshTokenLoginUsingToken() throws Exception {
//        Account account = TestUtil.createAccount();
//        DummyHttpResponse dummyHttpResponse = new DummyHttpResponse().withWritter(writerMock);
//        tokenService.createTokenCookies(dummyHttpResponse, account);
//        Set<Cookie> cookies = dummyHttpResponse.getCookies();
//        MockHttpServletResponse response = this.mvc.perform(get("/api/refresh").cookie(cookies.toArray(new Cookie[cookies.size()])))
//                .andExpect(status().is2xxSuccessful()).andReturn().getResponse();
//        assertThat(response.getCookie("AUTH-TOKEN")).isNotNull();
//    }

    @Test
    public void accessResourceUsingBasicAuth() throws Exception {
        byte[] encodedBytes = Base64Utils.encode((TestUtil.EMAIL + ":" + TestUtil.PASSWORD).getBytes());
        String authHeader = AUTHENTICATION_SCHEME + " " + new String(encodedBytes);
        this.mvc.perform(get("/api/resource")
                .header(AUTHORIZATION, authHeader))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void failToaccessResourceUsingBadBasicAuth() throws Exception {
        byte[] encodedBytes = Base64Utils.encode((TestUtil.EMAIL + ":wrong" + TestUtil.PASSWORD).getBytes());
        String authHeader = AUTHENTICATION_SCHEME + " " + new String(encodedBytes);
        this.mvc.perform(get("/api/resource")
                .header(AUTHORIZATION, authHeader))
                .andExpect(status().is4xxClientError());
    }


}

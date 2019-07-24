package com.qprogramming.shopper.app.api;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.account.devices.Device;
import com.qprogramming.shopper.app.account.devices.NewDevice;
import com.qprogramming.shopper.app.account.event.AccountEvent;
import com.qprogramming.shopper.app.login.RegisterForm;
import com.qprogramming.shopper.app.login.token.JwtAuthenticationRequest;
import com.qprogramming.shopper.app.login.token.TokenService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;

import java.io.PrintWriter;
import java.util.Optional;

import static com.qprogramming.shopper.app.filters.BasicRestAuthenticationFilter.AUTHENTICATION_SCHEME;
import static com.qprogramming.shopper.app.filters.BasicRestAuthenticationFilter.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthenticationControllerTest extends MockedAccountTestBase {


    public static final String PASS = "pass";
    @Autowired
    protected WebApplicationContext context;
    protected MockMvc mvc;
    protected MockMvc standaloneMvc;
    private AuthenticationController controller;

    @Mock
    private PrintWriter writerMock;

    @Autowired
    private TokenService tokenService;
    @Mock
    private AuthenticationManager authenticationManagerMock;
    @Mock
    private AccountService accountServiceMock;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithAnonymousUser
    public void shouldGetUnauthorizedWithoutRoleTest() throws Exception {
        this.mvc.perform(get("/api/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = TestUtil.EMAIL, userDetailsServiceBeanName = "accountService")
    public void getPersonsSuccessfullyWithUserRoleTest() throws Exception {
        this.mvc.perform(get("/api/account/whoami"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAllUserSuccessWithAdminRoleTest() throws Exception {
        this.mvc.perform(get("/api/account/all"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser
    public void getAllUserFailWithUserRoleTest() throws Exception {
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
    public void accessResourceUsingBasicAuthTest() throws Exception {
        byte[] encodedBytes = Base64Utils.encode((TestUtil.EMAIL + ":" + TestUtil.PASSWORD).getBytes());
        String authHeader = AUTHENTICATION_SCHEME + " " + new String(encodedBytes);
        this.mvc.perform(get("/api/resource")
                .header(AUTHORIZATION, authHeader))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void failToaccessResourceUsingBadBasicAuthTest() throws Exception {
        byte[] encodedBytes = Base64Utils.encode((TestUtil.EMAIL + ":wrong" + TestUtil.PASSWORD).getBytes());
        String authHeader = AUTHENTICATION_SCHEME + " " + new String(encodedBytes);
        this.mvc.perform(get("/api/resource")
                .header(AUTHORIZATION, authHeader))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void successfullyLoginUser() throws Exception {
        JwtAuthenticationRequest request = new JwtAuthenticationRequest();
        request.setUsername(TestUtil.EMAIL);
        request.setPassword(TestUtil.PASSWORD);
        this.mvc.perform(post("/auth")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void failToLoginUser() throws Exception {
        JwtAuthenticationRequest request = new JwtAuthenticationRequest();
        request.setUsername(TestUtil.EMAIL);
        request.setPassword(TestUtil.PASSWORD + 1);
        this.mvc.perform(post("/auth")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRegisterEmailExists() throws Exception {
        initMocked();
        RegisterForm form = new RegisterForm();
        form.setEmail(testAccount.getEmail());
        when(accountServiceMock.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));
        this.standaloneMvc.perform(post("/auth/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testRegisterPasswordsNotMaching() throws Exception {
        initMocked();
        RegisterForm form = new RegisterForm();
        form.setEmail(testAccount.getEmail());
        form.setPassword(TestUtil.PASSWORD);
        form.setConfirmPassword(TestUtil.PASSWORD + 1);
        when(accountServiceMock.findByEmail(testAccount.getEmail())).thenReturn(Optional.empty());
        this.standaloneMvc.perform(post("/auth/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testRegisterTooShort() throws Exception {
        initMocked();
        RegisterForm form = new RegisterForm();
        form.setEmail(testAccount.getEmail());
        form.setPassword(PASS);
        form.setConfirmPassword(PASS);
        when(accountServiceMock.findByEmail(testAccount.getEmail())).thenReturn(Optional.empty());
        this.standaloneMvc.perform(post("/auth/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        initMocked();
        RegisterForm form = new RegisterForm();
        form.setEmail(testAccount.getEmail());
        form.setPassword(TestUtil.PASSWORD + 1);
        form.setConfirmPassword(TestUtil.PASSWORD + 1);
        AccountEvent event = new AccountEvent();

        testAccount.setId(TestUtil.USER_RANDOM_ID);
        when(accountServiceMock.findByEmail(testAccount.getEmail())).thenReturn(Optional.empty());
        when(accountServiceMock.createLocalAccount(any(Account.class))).then(returnsFirstArg());
        when(accountServiceMock.createConfirmEvent(any(Account.class))).thenReturn(event);
        this.standaloneMvc.perform(post("/auth/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isOk());
        verify(accountServiceMock, times(1)).createLocalAccount(any(Account.class));
        verify(accountServiceMock, times(1)).sendConfirmEmail(any(Account.class), any(AccountEvent.class));
    }

    @Test
    public void testRegisterNewDeviceEmailNotFound() throws Exception {
        initMocked();
        when(accountServiceMock.findByEmail(testAccount.getEmail())).thenReturn(Optional.empty());
        this.standaloneMvc.perform(post("/auth/new-device")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(testAccount.getEmail()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRegisterNewDeviceSuccess() throws Exception {
        initMocked();
        NewDevice newDevice = new NewDevice(new Device(), "plainKey");
        newDevice.setId("ID");
        when(accountServiceMock.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));
        when(accountServiceMock.registerNewDevice(testAccount)).thenReturn(newDevice);
        MvcResult mvcResult = this.standaloneMvc.perform(post("/auth/new-device")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(testAccount.getEmail()))
                .andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        NewDevice result = TestUtil.convertJsonToObject(contentAsString, NewDevice.class);
        assertThat(result.getPlainKey()).isNotBlank();
        verify(accountServiceMock, times(1)).createConfirmDeviceEvent(testAccount, newDevice.getId());
    }

    private void initMocked() {
        super.setup();
        MockitoAnnotations.initMocks(this);
        controller = new AuthenticationController(tokenService, authenticationManagerMock, accountServiceMock);
        standaloneMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();


    }


}

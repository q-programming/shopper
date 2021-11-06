package com.qprogramming.shopper.app.api;

import com.fasterxml.uuid.Generators;
import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountPasswordEncoder;
import com.qprogramming.shopper.app.account.AccountRepository;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.account.devices.Device;
import com.qprogramming.shopper.app.account.devices.NewDevice;
import com.qprogramming.shopper.app.account.event.AccountEvent;
import com.qprogramming.shopper.app.account.event.AccountEventType;
import com.qprogramming.shopper.app.exceptions.DeviceNotFoundException;
import com.qprogramming.shopper.app.login.RegisterForm;
import com.qprogramming.shopper.app.login.token.JwtAuthenticationRequest;
import com.qprogramming.shopper.app.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;

import java.io.PrintWriter;
import java.util.Optional;

import static com.qprogramming.shopper.app.security.BasicRestAuthenticationFilter.AUTHENTICATION_SCHEME;
import static com.qprogramming.shopper.app.security.BasicRestAuthenticationFilter.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class AuthenticationControllerTest extends MockedAccountTestBase {


    public static final String PASS = "pass";
    @Autowired
    protected WebApplicationContext context;
    @Autowired
    private AccountRepository accountRepository;
    protected MockMvc mvc;
    protected MockMvc standaloneMvc;
    private AuthenticationController controller;
    @Mock
    private PrintWriter writerMock;
    @Autowired
    private AccountPasswordEncoder accountPasswordEncoder;
    @Autowired
    private TokenService tokenService;
    @Mock
    private AuthenticationManager authenticationManagerMock;
    @Mock
    private AccountService accountServiceMock;


    @BeforeEach
    void setUp() {
        super.setup();
        accountRepository.deleteAll();
        testAccount.setPassword(accountPasswordEncoder.encode(testAccount.getPassword()));
        accountRepository.save(testAccount);
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithAnonymousUser
    void shouldGetUnauthorizedWithoutRoleTest() throws Exception {
        this.mvc.perform(get("/api/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUserSuccessWithAdminRoleTest() throws Exception {
        this.mvc.perform(get("/api/account/all"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser
    void getAllUserFailWithUserRoleTest() throws Exception {
        this.mvc.perform(get("/api/account/all"))
                .andExpect(status().is4xxClientError());
    }

    //TODO check why it's not working in run all tests
//    @Test
//    void refreshTokenLoginUsingToken() throws Exception {
//        Account account = TestUtil.createAccount();
//        DummyHttpResponse dummyHttpResponse = new DummyHttpResponse().withWritter(writerMock);
//        tokenService.createTokenCookies(dummyHttpResponse, account);
//        Set<Cookie> cookies = dummyHttpResponse.getCookies();
//        MockHttpServletResponse response = this.mvc.perform(get("/api/refresh").cookie(cookies.toArray(new Cookie[cookies.size()])))
//                .andExpect(status().is2xxSuccessful()).andReturn().getResponse();
//        assertThat(response.getCookie("AUTH-TOKEN")).isNotNull();
//    }


    @Test
    void failToaccessResourceUsingBadBasicAuthTest() throws Exception {
        when(authMock.getPrincipal())
                .thenReturn(null);
        byte[] encodedBytes = Base64Utils.encode((TestUtil.EMAIL + ":wrong" + TestUtil.PASSWORD).getBytes());
        String authHeader = AUTHENTICATION_SCHEME + " " + new String(encodedBytes);
        this.mvc.perform(get("/api/resource")
                .header(AUTHORIZATION, authHeader))
                .andExpect(status().is4xxClientError());
    }

//    @Test
//    void successfullyLoginUser() throws Exception {
//        JwtAuthenticationRequest request = new JwtAuthenticationRequest();
//        request.setUsername(TestUtil.EMAIL);
//        request.setPassword(TestUtil.PASSWORD);
//        this.mvc.perform(post("/auth")
//                .contentType(TestUtil.APPLICATION_JSON_UTF8)
//                .content(TestUtil.convertObjectToJsonBytes(request)))
//                .andExpect(status().isOk());
//    }

    @Test
    void failToLoginUser() throws Exception {
        JwtAuthenticationRequest request = new JwtAuthenticationRequest();
        request.setUsername(TestUtil.EMAIL);
        request.setPassword(TestUtil.PASSWORD + 1);
        this.mvc.perform(post("/auth")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegisterEmailExists() throws Exception {
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
    void testRegisterPasswordsNotMaching() throws Exception {
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
    void testRegisterTooShort() throws Exception {
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
    void testRegisterSuccess() throws Exception {
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
    void testRegisterNewDeviceEmailNotFound() throws Exception {
        initMocked();
        RegisterForm form = new RegisterForm();
        form.setEmail(testAccount.getEmail());
        when(accountServiceMock.findByEmail(testAccount.getEmail())).thenReturn(Optional.empty());
        this.standaloneMvc.perform(post("/auth/new-device")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRegisterNewDeviceSuccess() throws Exception {
        initMocked();
        RegisterForm form = new RegisterForm();
        form.setEmail(testAccount.getEmail());
        String name = "name";
        form.setName(name);
        NewDevice newDevice = new NewDevice(new Device(), "plainKey", testAccount.getEmail());
        newDevice.setId("ID");
        when(accountServiceMock.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));
        when(accountServiceMock.registerNewDevice(testAccount, name)).thenReturn(newDevice);
        MvcResult mvcResult = this.standaloneMvc.perform(post("/auth/new-device")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(form)))
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

    @Test
    void testConfirmEventNotFound() throws Exception {
        initMocked();
        String token = Generators.timeBasedGenerator().generate().toString();
        when(accountServiceMock.findEvent(token)).thenReturn(Optional.empty());
        this.standaloneMvc.perform(post("/auth/confirm")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testConfirmTokenExpired() throws Exception {
        initMocked();
        String token = "09011a27-478c-11e7-bcf7-930b1424157e";
        AccountEvent event = new AccountEvent();
        event.setToken(token);
        when(accountServiceMock.findEvent(token)).thenReturn(Optional.of(event));
        this.standaloneMvc.perform(post("/auth/confirm")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(token))
                .andExpect(status().isConflict());
        verify(accountServiceMock, times(1)).removeEvent(event);
    }

    @Test
    void testConfirmAccountNotMatching() throws Exception {
        initMocked();
        String token = Generators.timeBasedGenerator().generate().toString();
        AccountEvent event = new AccountEvent();
        event.setToken(token);
        event.setType(AccountEventType.DEVICE_CONFIRM);
        event.setAccount(TestUtil.createAccount("John", "Doe"));
        when(accountServiceMock.findEvent(token)).thenReturn(Optional.of(event));
        this.standaloneMvc.perform(post("/auth/confirm")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testConfirmRegisterNewDeviceNotFound() throws Exception {
        initMocked();
        NewDevice newDevice = new NewDevice(new Device(), "plainKey", testAccount.getEmail());
        newDevice.setId("ID");
        String token = Generators.timeBasedGenerator().generate().toString();
        AccountEvent event = new AccountEvent();
        event.setToken(token);
        event.setData(newDevice.getId());
        event.setType(AccountEventType.DEVICE_CONFIRM);
        event.setAccount(testAccount);
        when(accountServiceMock.findEvent(token)).thenReturn(Optional.of(event));
        doThrow(new DeviceNotFoundException()).when(accountServiceMock).confirmDevice(testAccount, event.getData());
        this.standaloneMvc.perform(post("/auth/confirm")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(token))
                .andExpect(status().isNotFound());

    }

    @Test
    void testConfirmRegisterNewDeviceSuccess() throws Exception {
        initMocked();
        NewDevice newDevice = new NewDevice(new Device(), "plainKey", testAccount.getEmail());
        newDevice.setId("ID");
        String token = Generators.timeBasedGenerator().generate().toString();
        AccountEvent event = new AccountEvent();
        event.setToken(token);
        event.setData(newDevice.getId());
        event.setType(AccountEventType.DEVICE_CONFIRM);
        event.setAccount(testAccount);
        when(accountServiceMock.findEvent(token)).thenReturn(Optional.of(event));
        this.standaloneMvc.perform(post("/auth/confirm")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(token))
                .andExpect(status().isOk());
        verify(accountServiceMock, times(1)).confirmDevice(testAccount, event.getData());
    }


}

package com.qprogramming.shopper.app.api;

import com.fasterxml.uuid.Generators;
import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.*;
import com.qprogramming.shopper.app.account.devices.Device;
import com.qprogramming.shopper.app.account.devices.NewDevice;
import com.qprogramming.shopper.app.account.event.AccountEvent;
import com.qprogramming.shopper.app.account.event.AccountEventRepository;
import com.qprogramming.shopper.app.account.event.AccountEventType;
import com.qprogramming.shopper.app.exceptions.DeviceNotFoundException;
import com.qprogramming.shopper.app.login.RegisterForm;
import com.qprogramming.shopper.app.login.token.JwtAuthenticationRequest;
import com.qprogramming.shopper.app.login.token.UserTokenState;
import com.qprogramming.shopper.app.security.TokenService;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static com.qprogramming.shopper.app.security.DeviceRestAuthenticationFilter.AUTHENTICATION_SCHEME;
import static com.qprogramming.shopper.app.security.DeviceRestAuthenticationFilter.AUTHORIZATION;
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
    private static final String API_REFRESH = "/api/refresh";
    private static final String AUTH_PASSWORD_RESET = "/auth/password-reset";
    private static final String AUTH_PASSWORD_CHANGE = "/auth/password-change";

    @Autowired
    protected WebApplicationContext context;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountEventRepository accountEventRepository;
    protected MockMvc mvc;
    protected MockMvc standaloneMvc;
    private AuthenticationController controller;
    @Autowired
    private AccountPasswordEncoder accountPasswordEncoder;
    @Mock
    private TokenService tokenServiceMock;
    @Mock
    private AuthenticationManager authenticationManagerMock;
    @Mock
    private AccountService accountServiceMock;
    @SpyBean
    private AccountService accountServiceSpy;


    @BeforeEach
    void setUp() {
        super.setup();
        accountEventRepository.deleteAll();
        accountRepository.deleteAll();
        testAccount.setPassword(accountPasswordEncoder.encode(testAccount.getPassword()));
        accountRepository.save(testAccount);
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private void initMocked() {
        super.setup();
        MockitoAnnotations.openMocks(this);
        controller = new AuthenticationController(tokenServiceMock, authenticationManagerMock, accountServiceMock);
        standaloneMvc = MockMvcBuilders.standaloneSetup(controller)
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

    @Test
    void refreshAuthenticationTokenNoTokenTest() throws Exception {
        initMocked();
        val mvcResult = standaloneMvc.perform(get(API_REFRESH)).andExpect(status().isAccepted()).andReturn();
        val contentAsString = mvcResult.getResponse().getContentAsString();
        val result = TestUtil.convertJsonToObject(contentAsString, UserTokenState.class);
        assertThat(result.getExpires_in()).isEqualTo(-1L);
    }

    @Test
    void refreshAuthenticationTokenExpiredTest() throws Exception {
        initMocked();
        val token = "TOKEN";
        when(tokenServiceMock.getToken(any())).thenReturn(token);
        when(tokenServiceMock.canTokenBeRefreshed(token)).thenReturn(false);
        val mvcResult = standaloneMvc.perform(get(API_REFRESH)).andExpect(status().isAccepted()).andReturn();
        val contentAsString = mvcResult.getResponse().getContentAsString();
        val result = TestUtil.convertJsonToObject(contentAsString, UserTokenState.class);
        assertThat(result.getExpires_in()).isEqualTo(-1L);
    }

    @Test
    void refreshAuthenticationTest() throws Exception {
        initMocked();
        val token = "TOKEN";
        when(tokenServiceMock.getToken(any())).thenReturn(token);
        when(tokenServiceMock.canTokenBeRefreshed(token)).thenReturn(true);
        when(tokenServiceMock.refreshToken(token)).then(returnsFirstArg());
        standaloneMvc.perform(get(API_REFRESH)).andExpect(status().isOk());
        verify(tokenServiceMock, times(1)).refreshCookie(anyString(), any());
    }


    @Test
    void failToAccessResourceUsingBadBasicAuthTest() throws Exception {
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

    @Test
    @WithAnonymousUser
    void testPasswordReset() throws Exception {
        doNothing().when(accountServiceSpy).sendConfirmEmail(any(Account.class), any(AccountEvent.class));
        this.mvc.perform(post(AUTH_PASSWORD_RESET).contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(testAccount.getEmail()))
                .andExpect(status().isOk());
        val eventCaptor = ArgumentCaptor.forClass(AccountEvent.class);
        val accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountServiceSpy).sendConfirmEmail(accountCaptor.capture(), eventCaptor.capture());
        val event = eventCaptor.getValue();
        assertThat(event.getAccount()).isEqualTo(testAccount);
        assertThat(event.getType()).isEqualTo(AccountEventType.PASSWORD_RESET);
    }

    @Test
    @WithAnonymousUser
    void testPasswordWrongPassChange() throws Exception {
        val event = accountServiceSpy.createPasswordResetEvent(testAccount);
        val passwordForm = PasswordForm.builder().password("pass").confirmpassword("pass2").token(event.getToken()).build();
        this.mvc.perform(post(AUTH_PASSWORD_CHANGE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(passwordForm)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithAnonymousUser
    void testPasswordChange() throws Exception {
        val event = accountServiceSpy.createPasswordResetEvent(testAccount);
        val passwordForm = PasswordForm.builder().password("pass").confirmpassword("pass").token(event.getToken()).build();
        this.mvc.perform(post(AUTH_PASSWORD_CHANGE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(passwordForm)))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testPasswordChangeWrongToken() throws Exception {
        val token = accountServiceSpy.generateToken();
        val passwordForm = PasswordForm.builder().password("pass").confirmpassword("pass").token(token).build();
        this.mvc.perform(post(AUTH_PASSWORD_CHANGE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(passwordForm)))
                .andExpect(status().isNotFound());
    }
}

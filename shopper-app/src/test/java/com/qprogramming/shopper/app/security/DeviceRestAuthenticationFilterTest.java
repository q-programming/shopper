package com.qprogramming.shopper.app.security;

import com.nimbusds.jose.util.Base64;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountPasswordEncoder;
import com.qprogramming.shopper.app.account.AccountRepository;
import com.qprogramming.shopper.app.account.devices.Device;
import com.qprogramming.shopper.app.config.MockSecurityContext;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Optional;

import static com.qprogramming.shopper.app.TestUtil.PASSWORD;
import static com.qprogramming.shopper.app.TestUtil.convertJsonToObject;
import static com.qprogramming.shopper.app.security.DeviceRestAuthenticationFilter.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class DeviceRestAuthenticationFilterTest {

    private static final String API_ACCOUNT_WHOAMI = "/api/account/whoami";
    @Autowired
    protected WebApplicationContext context;
    protected MockMvc mvc;
    @Mock
    protected MockSecurityContext securityMock;
    @SpyBean
    private AccountPasswordEncoder accountPasswordEncoder;
    @MockBean
    private AccountRepository accountRepositoryMock;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = TestUtil.createAccount();
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        when(securityMock.getAuthentication()).thenReturn(null);
    }

    @Test
    @DisplayName("There is device, but it has not yet been confirmed")
    void deviceNotYetConfirmedTest() throws Exception {
        SecurityContextHolder.setContext(securityMock);
        val device = Device.builder()
                .id("1")
                .deviceKey(PASSWORD)
                .build();
        testAccount.setDevices(Collections.singleton(device));
        doReturn(true).when(accountPasswordEncoder).matches(any(), any());
        when(accountRepositoryMock.findOneByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));
        when(accountRepositoryMock.save(any())).then(returnsFirstArg());
        val auth = Base64.encode(testAccount.getEmail() + ":" + device.getDeviceKey());
        this.mvc.perform(get(API_ACCOUNT_WHOAMI).header(AUTHORIZATION, auth))
                .andExpect(status().is(HttpStatus.LOCKED.value()));
    }

    @Test
    @DisplayName("There is device and is confirmed")
    void deviceConfirmedTest() throws Exception {
        val device = Device.builder()
                .id("1")
                .deviceKey(PASSWORD)
                .enabled(true)
                .build();
        testAccount.setDevices(Collections.singleton(device));
        doReturn(true).when(accountPasswordEncoder).matches(any(), any());
        when(accountRepositoryMock.findOneByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));
        when(accountRepositoryMock.save(any())).then(returnsFirstArg());
        val auth = Base64.encode(testAccount.getEmail() + ":" + device.getDeviceKey());
        val mvcStringResponse = this.mvc.perform(get(API_ACCOUNT_WHOAMI).header(AUTHORIZATION, auth))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        val result = convertJsonToObject(mvcStringResponse, Account.class);
        assertThat(result).isEqualTo(testAccount);
    }

    @Test
    @DisplayName("Account was not yet confirmed")
    void accountNotConfirmedTest() throws Exception {
        val device = Device.builder()
                .id("1")
                .deviceKey(PASSWORD)
                .enabled(true)
                .build();
        testAccount.setDevices(Collections.singleton(device));
        testAccount.setEnabled(false);
        when(accountRepositoryMock.findOneByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));
        val auth = Base64.encode(testAccount.getEmail() + ":" + device.getDeviceKey());
        this.mvc.perform(get(API_ACCOUNT_WHOAMI).header(AUTHORIZATION, auth))
                .andExpect(status().is(HttpStatus.LOCKED.value()));
    }

}
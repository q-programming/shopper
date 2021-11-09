package com.qprogramming.shopper.app.api.account;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.account.devices.Device;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.DeviceNotFoundException;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountRestControllerTest extends MockedAccountTestBase {

    private static final String API_FRIENDS_URL = "/api/account/friends";
    private static final String API_ACCOUNT_URL = "/api/account/";
    private static final String API_DEVICES_URL = "/api/account/settings/devices";
    private static final String API_SETTINGS_URL = "/api/account/settings/";

    @Mock
    private AccountService accountServiceMock;
    @Mock
    private ShoppingListService shoppingListServiceMock;
    @Mock
    private LogoutHandler logoutHandlerMock;


    @BeforeEach
    void setUp() {
        super.setup();
        AccountRestController controller = new AccountRestController(accountServiceMock, shoppingListServiceMock, logoutHandlerMock);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }


    @Test
    void getFriendListAccountNotFound() throws Exception {
        when(accountServiceMock.getAllFriendList()).thenThrow(AccountNotFoundException.class);
        this.mvc.perform(get(API_FRIENDS_URL)).andExpect(status().is4xxClientError());
    }

    @Test
    void getFriendList() throws Exception {
        Account account = TestUtil.createAccount("John", "Doe");
        when(accountServiceMock.getAllFriendList()).thenReturn(Collections.singleton(account));
        MvcResult mvcResult = this.mvc.perform(get(API_FRIENDS_URL)).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Set<Account> result = TestUtil.convertJsonToSet(contentAsString, Set.class, Account.class);
        assertThat(result.contains(account)).isTrue();
    }

    @Test
    void getFriendListWithTerm() throws Exception {
        Account account = TestUtil.createAccount("John", "Doe");
        when(accountServiceMock.getAllFriendList()).thenReturn(Collections.singleton(account));
        MvcResult mvcResult = this.mvc.perform(get(API_FRIENDS_URL).param("term", "user")).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Set<Account> result = TestUtil.convertJsonToSet(contentAsString, Set.class, Account.class);
        assertThat(result.contains(account)).isTrue();
    }

    @Test
    void getFriendListWithTermNotFound() throws Exception {
        Account account = TestUtil.createAccount("John", "Doe");
        when(accountServiceMock.getAllFriendList()).thenReturn(Collections.singleton(account));
        MvcResult mvcResult = this.mvc.perform(get(API_FRIENDS_URL).param("term", "john")).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Set<Account> result = TestUtil.convertJsonToSet(contentAsString, Set.class, Account.class);
        assertThat(result.contains(account)).isFalse();
    }

    @Test
    void getDeviceList() throws Exception {
        Device device = new Device();
        device.setId("1");
        device.setName("name");
        device.setDeviceKey("key");
        device.setLastUsed(new Date());
        testAccount.getDevices().add(device);
        when(accountServiceMock.findById(testAccount.getId())).thenReturn(testAccount);
        MvcResult mvcResult = this.mvc.perform(get(API_DEVICES_URL)).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Set<Device> devices = TestUtil.convertJsonToSet(contentAsString, Set.class, Device.class);
        assertThat(devices.contains(device)).isTrue();
    }

    @Test
    void getDeviceListNotFoundTest() throws Exception {
        when(accountServiceMock.findById(testAccount.getId())).thenThrow(AccountNotFoundException.class);
        this.mvc.perform(get(API_DEVICES_URL)).andExpect(status().is4xxClientError());
    }

    @Test
    void removeDeviceNotFound() throws Exception {
        doThrow(new DeviceNotFoundException()).when(accountServiceMock).removeDevice(anyString());
        this.mvc.perform(delete(API_DEVICES_URL + "/1/remove")).andExpect(status().isNotFound());
    }

    @Test
    void removeDevice() throws Exception {
        this.mvc.perform(delete(API_DEVICES_URL + "/1/remove")).andExpect(status().is2xxSuccessful());
    }

    @Test
    void getAvatarNotFoundTest() throws Exception {
        when(accountServiceMock.findById(testAccount.getId())).thenThrow(AccountNotFoundException.class);
        this.mvc.perform(get(API_ACCOUNT_URL + testAccount.getId() + "/avatar")).andExpect(status().is4xxClientError());
    }

    @Test
    void getAvatarTest() throws Exception {
        when(accountServiceMock.findById(testAccount.getId())).thenReturn(testAccount);
        this.mvc.perform(get(API_ACCOUNT_URL + testAccount.getId() + "/avatar")).andExpect(status().is2xxSuccessful());
        verify(accountServiceMock, times(1)).getAccountAvatar(testAccount);
    }

    @Test
    void getAllUsersTest() throws Exception {
        when(accountServiceMock.findById(testAccount.getId())).thenReturn(testAccount);
        val resultString = this.mvc.perform(post(API_ACCOUNT_URL + "/users")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(new String[]{testAccount.getId()})))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        val result = TestUtil.convertJsonToSet(resultString, Set.class, Account.class);
        assertThat(result)
                .isNotNull()
                .isNotEmpty();
        val account = result.iterator().next();
        assertEquals(account.getId(), testAccount.getId());
    }

    @Test
    void getAllUsersNotFoundTest() throws Exception {
        when(accountServiceMock.findById(testAccount.getId())).thenThrow(AccountNotFoundException.class);
        val resultString = this.mvc.perform(post(API_ACCOUNT_URL + "/users")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(new String[]{testAccount.getId()})))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        val result = TestUtil.convertJsonToSet(resultString, Set.class, Account.class);
        assertThat(result).isEmpty();
    }

    @Test
    void updateAvatarNotFoundTest() throws Exception {
        when(authMock.getPrincipal()).thenReturn(null);
        this.mvc.perform(post(API_ACCOUNT_URL + "/avatar-upload")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes("avatar")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateAvatarTest() throws Exception {
        this.mvc.perform(post(API_ACCOUNT_URL + "/avatar-upload")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(TestUtil.IMAGE_BASE64)))
                .andExpect(status().is2xxSuccessful());
        verify(accountServiceMock, times(1)).updateAvatar(any(Account.class), any());
    }

    @Test
    void whoamITest() throws Exception {
        val resultString = this.mvc.perform(get(API_ACCOUNT_URL + "/whoami"))
                .andExpect(status().is2xxSuccessful()).andReturn().getResponse().getContentAsString();
        val account = TestUtil.convertJsonToObject(resultString, Account.class);
        assertEquals(account.getId(), testAccount.getId());
    }

    @Test
    void changeLanguageTest() throws Exception {
        val language = "pl";
        this.mvc.perform(post(API_SETTINGS_URL + "/language")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(language))
                .andExpect(status().is2xxSuccessful());
        val captor = ArgumentCaptor.forClass(Account.class);
        verify(accountServiceMock).update(captor.capture());
        val result = captor.getValue();
        assertThat(result.getLanguage()).isEqualTo(language);
    }

    @Test
    void changeRightModeTest() throws Exception {
        this.mvc.perform(post(API_SETTINGS_URL + "/rightmode")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(true)))
                .andExpect(status().is2xxSuccessful());
        val captor = ArgumentCaptor.forClass(Account.class);
        verify(accountServiceMock).update(captor.capture());
        val result = captor.getValue();
        assertThat(result.isRighcheckbox()).isTrue();
    }

    @Test
    void deleteWrongAccountTest() throws Exception {
        val account = TestUtil.createAccount("Johny ", "Target");
        this.mvc.perform(delete(API_ACCOUNT_URL + "/delete")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(account)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteAccountTest() throws Exception {
        this.mvc.perform(delete(API_ACCOUNT_URL + "/delete")
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(testAccount)))
                .andExpect(status().isOk());
        verify(shoppingListServiceMock, times(1)).transferSharedListOwnership(testAccount);
        verify(shoppingListServiceMock, times(1)).deleteUserLists(testAccount);
        verify(logoutHandlerMock, times(1)).logout(any(), any(), any());
        verify(accountServiceMock, times(1)).delete(testAccount);
    }


}
package com.qprogramming.shopper.app.api.account;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.account.devices.Device;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.DeviceNotFoundException;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountRestControllerTest extends MockedAccountTestBase {

    private static final String API_FRIENDS_URL = "/api/account/friends";
    private static final String API_DEVICES_URL = "/api/account/settings/devices";

    @Mock
    private AccountService accountServiceMock;
    @Mock
    private ShoppingListService shoppingListServiceMock;
    @Mock
    private LogoutHandler logoutHandlerMock;


    @Before
    @Override
    public void setup() {
        super.setup();
        AccountRestController controller = new AccountRestController(accountServiceMock, shoppingListServiceMock, logoutHandlerMock);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }


    @Test
    public void getFriendListAccountNotFound() throws Exception {
        when(accountServiceMock.getAllFriendList()).thenThrow(AccountNotFoundException.class);
        this.mvc.perform(get(API_FRIENDS_URL)).andExpect(status().is4xxClientError());
    }

    @Test
    public void getFriendList() throws Exception {
        Account account = TestUtil.createAccount("John", "Doe");
        when(accountServiceMock.getAllFriendList()).thenReturn(Collections.singleton(account));
        MvcResult mvcResult = this.mvc.perform(get(API_FRIENDS_URL)).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Set<Account> result = TestUtil.convertJsonToSet(contentAsString, Set.class, Account.class);
        assertThat(result.contains(account)).isTrue();
    }

    @Test
    public void getFriendListWithTerm() throws Exception {
        Account account = TestUtil.createAccount("John", "Doe");
        when(accountServiceMock.getAllFriendList()).thenReturn(Collections.singleton(account));
        MvcResult mvcResult = this.mvc.perform(get(API_FRIENDS_URL).param("term", "user")).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Set<Account> result = TestUtil.convertJsonToSet(contentAsString, Set.class, Account.class);
        assertThat(result.contains(account)).isTrue();
    }

    @Test
    public void getFriendListWithTermNotFound() throws Exception {
        Account account = TestUtil.createAccount("John", "Doe");
        when(accountServiceMock.getAllFriendList()).thenReturn(Collections.singleton(account));
        MvcResult mvcResult = this.mvc.perform(get(API_FRIENDS_URL).param("term", "john")).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Set<Account> result = TestUtil.convertJsonToSet(contentAsString, Set.class, Account.class);
        assertThat(result.contains(account)).isFalse();
    }

    @Test
    public void getDeviceList() throws Exception {
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
    public void removeDeviceNotFound() throws Exception {
        doThrow(new DeviceNotFoundException()).when(accountServiceMock).removeDevice(anyString());
        this.mvc.perform(delete(API_DEVICES_URL + "/1/remove")).andExpect(status().isNotFound());
    }

    @Test
    public void removeDevice() throws Exception {
        this.mvc.perform(delete(API_DEVICES_URL + "/1/remove")).andExpect(status().is2xxSuccessful());
    }


}
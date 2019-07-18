package com.qprogramming.shopper.app.api.account;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountRestControllerTest extends MockedAccountTestBase {

    private static final String API_FRIENDS_URL = "/api/account/friends";

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


}
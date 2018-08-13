package com.qprogramming.shopper.app.api.shoppinglist;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.BadProductNameException;
import com.qprogramming.shopper.app.items.ListItem;
import com.qprogramming.shopper.app.items.ListItemRepository;
import com.qprogramming.shopper.app.items.ListItemService;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListRepository;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
public class ShoppingListRestControllerTest extends MockedAccountTestBase {

    private static final String NAME = "name";
    private static final String JOHN = "John";
    private static final String DOE = "Doe";
    private static final String API_LIST_URL = "/api/list/";
    private static final String SHARE = "/share";
    private static final String STOP_SHARING = "/stop-sharing";
    private static final String ARCHIVE = "/archive";
    private static final String DELETE = "/delete";
    private static final String MINE = "mine";
    private static final String USER = "user/";
    private static final String ITEM_ADD = "/item-add";

    @Mock
    private ShoppingListRepository listRepositoryMock;
    @Mock
    private AccountService accountServiceMock;
    @Mock
    private ProductRepository productRepositoryMock;
    @Mock
    private ListItemRepository listItemRepositoryMock;

    private ListItemService listItemService;

    private ShoppingListService listService;
    private ShoppingListRestController controller;

    @Before
    @Override
    public void setup() {
        super.setup();
        listService = new ShoppingListService(listRepositoryMock, accountServiceMock);
        listItemService = new ListItemService(listItemRepositoryMock, productRepositoryMock);
        controller = new ShoppingListRestController(listService, listItemService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }

    @Test
    @WithUserDetails(value = TestUtil.EMAIL, userDetailsServiceBeanName = "accountService")
    public void getCurrentUserLists() throws Exception {
        ShoppingList list1 = createList(NAME, 1L);
        ShoppingList list2 = createList(NAME, 2L);
        Set<ShoppingList> shoppingList = Stream.of(list1, list2).collect(Collectors.toSet());
        when(listRepositoryMock.findAllByOwnerIdOrSharedIn(anyString(), anySet())).thenReturn(shoppingList);
        when(accountServiceMock.findById(testAccount.getId())).thenReturn(testAccount);
        MvcResult mvcResult = this.mvc.perform(get(API_LIST_URL + MINE))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<ShoppingList> result = TestUtil.convertJsonToList(jsonResponse, List.class, ShoppingList.class);
        assertThat(result.containsAll(shoppingList)).isTrue();
    }

    @Test
    public void getCurrentUserListsWithShared() throws Exception {
        Account account = TestUtil.createAccount(JOHN, DOE);
        account.setId(TestUtil.ADMIN_RANDOM_ID);
        ShoppingList list1 = createList(NAME, 1L);
        ShoppingList list2 = createList(NAME, 2L);
        ShoppingList shared = createList(NAME, 3L);
        shared.setOwnerId(account.getId());
        shared.getShared().add(testAccount.getId());
        Set<ShoppingList> shoppingList = Stream.of(list1, list2, shared).collect(Collectors.toSet());
        when(listRepositoryMock.findAllByOwnerIdOrSharedIn(anyString(), anySet())).thenReturn(shoppingList);
        when(accountServiceMock.findById(testAccount.getId())).thenReturn(testAccount);
        when(accountServiceMock.findById(account.getId())).thenReturn(account);
        MvcResult mvcResult = this.mvc.perform(get(API_LIST_URL + MINE))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<ShoppingList> result = TestUtil.convertJsonToList(jsonResponse, List.class, ShoppingList.class);
        assertThat(result.containsAll(shoppingList)).isTrue();
        assertThat(result.size() == 3).isTrue();
    }

    @Test
    public void getCurrentUserListsWithSharedNoOwnerInDB() throws Exception {
        Account account = TestUtil.createAccount(JOHN, DOE);
        account.setId(TestUtil.ADMIN_RANDOM_ID);
        ShoppingList list1 = createList(NAME, 1L);
        ShoppingList list2 = createList(NAME, 2L);
        ShoppingList shared = createList(NAME, 3L);
        shared.setOwnerId(account.getId());
        shared.getShared().add(testAccount.getId());
        Set<ShoppingList> shoppingList = Stream.of(list1, list2, shared).collect(Collectors.toSet());
        when(listRepositoryMock.findAllByOwnerIdOrSharedIn(anyString(), anySet())).thenReturn(shoppingList);
        when(accountServiceMock.findById(testAccount.getId())).thenThrow(new AccountNotFoundException());
        this.mvc.perform(get(API_LIST_URL + MINE))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getUserListsNotFound() throws Exception {
        when(accountServiceMock.findById(testAccount.getId())).thenThrow(new AccountNotFoundException());
        this.mvc.perform(get(API_LIST_URL + USER + testAccount.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getUserListsWithSharedForCurrentUser() throws Exception {
        Account account = TestUtil.createAccount(JOHN, DOE);
        account.setId(TestUtil.ADMIN_RANDOM_ID);
        ShoppingList otherlist = createList(NAME, 1L);
        ShoppingList list2 = createList(NAME, 2L);
        ShoppingList notVisibleList = createList(NAME, 3L);
        notVisibleList.setOwnerId(account.getId());
        otherlist.setOwnerId(account.getId());
        otherlist.getShared().add(testAccount.getId());
        Set<ShoppingList> shoppingList = Stream.of(otherlist, list2, notVisibleList).collect(Collectors.toSet());
        when(listRepositoryMock.findAllByOwnerIdOrSharedIn(anyString(), anySet())).thenReturn(shoppingList);
        when(accountServiceMock.findById(testAccount.getId())).thenReturn(testAccount);
        when(accountServiceMock.findById(account.getId())).thenReturn(account);
        MvcResult mvcResult = this.mvc.perform(get(API_LIST_URL + USER + account.getId()))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<ShoppingList> result = TestUtil.convertJsonToList(jsonResponse, List.class, ShoppingList.class);
        assertThat(result.size() == 2).isTrue();
        assertThat(result.contains(notVisibleList)).isFalse();
    }


    @Test
    public void addNewList() throws Exception {
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        MvcResult mvcResult = this.mvc.perform(post(API_LIST_URL + "add").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(NAME))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getOwnerId()).isEqualTo(TestUtil.USER_RANDOM_ID);
    }

    @Test
    public void getList() throws Exception {
        ShoppingList list1 = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list1));
        MvcResult mvcResult = this.mvc.perform(get(API_LIST_URL + 1))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getOwnerId()).isEqualTo(TestUtil.USER_RANDOM_ID);
    }

    @Test
    public void getListNotFound() throws Exception {
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        this.mvc.perform(get(API_LIST_URL + 1))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getListNoPermission() throws Exception {
        ShoppingList list1 = createList(NAME, 1L);
        list1.setOwnerId(NAME);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list1));
        this.mvc.perform(get(API_LIST_URL + 1))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shareList() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        Account account = TestUtil.createAccount(JOHN, DOE);
        account.setId(TestUtil.ADMIN_RANDOM_ID);
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        when(listRepositoryMock.findById(list.getId())).thenReturn(Optional.of(list));

        when(accountServiceMock.findById(account.getId())).thenReturn(account);
        MvcResult mvcResult = this.mvc.perform(post(API_LIST_URL + list.getId() + SHARE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(account.getId()))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getShared().contains(account.getId())).isTrue();
    }

    @Test
    public void shareListNotFound() throws Exception {
        this.mvc.perform(post(API_LIST_URL + 1L + SHARE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shareListAccountNotFound() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        when(listRepositoryMock.findById(list.getId())).thenReturn(Optional.of(list));
        when(accountServiceMock.findById(TestUtil.ADMIN_RANDOM_ID)).thenThrow(new AccountNotFoundException());
        this.mvc.perform(post(API_LIST_URL + 1L + SHARE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void stopSharingList() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        Account account = TestUtil.createAccount(JOHN, DOE);
        account.setId(TestUtil.ADMIN_RANDOM_ID);
        list.getShared().add(account.getId());
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        when(listRepositoryMock.findById(list.getId())).thenReturn(Optional.of(list));
        when(accountServiceMock.findById(account.getId())).thenReturn(account);
        MvcResult mvcResult = this.mvc.perform(post(API_LIST_URL + list.getId() + STOP_SHARING).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(account.getId()))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getShared().contains(account.getId())).isFalse();
    }

    @Test
    public void listOperationsWithoutPermission() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        Account account = TestUtil.createAccount(JOHN, DOE);
        account.setId(TestUtil.ADMIN_RANDOM_ID);
        list.setOwnerId(account.getId());
        when(listRepositoryMock.findById(list.getId())).thenReturn(Optional.of(list));
        this.mvc.perform(post(API_LIST_URL + 1L + SHARE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().is4xxClientError());
        this.mvc.perform(post(API_LIST_URL + 1L + STOP_SHARING).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().is4xxClientError());
        this.mvc.perform(post(API_LIST_URL + 1L + DELETE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().is4xxClientError());
        this.mvc.perform(post(API_LIST_URL + 1L + ARCHIVE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().is4xxClientError());

    }

    @Test
    public void archiveListNotFound() throws Exception {
        this.mvc.perform(post(API_LIST_URL + 1L + ARCHIVE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteListNotFound() throws Exception {
        this.mvc.perform(post(API_LIST_URL + 1L + DELETE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void archiveList() throws Exception {
        ShoppingList list1 = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list1));
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        MvcResult mvcResult = this.mvc.perform(post(API_LIST_URL + 1 + ARCHIVE))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.isArchived()).isTrue();
    }

    @Test
    public void deleteList() throws Exception {
        ShoppingList list1 = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list1));
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        this.mvc.perform(post(API_LIST_URL + 1 + DELETE))
                .andExpect(status().is2xxSuccessful());
        verify(listRepositoryMock, times(1)).delete(list1);
    }

    @Test
    public void archiveSharedList() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        Account account = TestUtil.createAccount(JOHN, DOE);
        account.setId(TestUtil.ADMIN_RANDOM_ID);
        list.setOwnerId(account.getId());
        list.getShared().add(testAccount.getId());
        when(listRepositoryMock.findById(list.getId())).thenReturn(Optional.of(list));
        when(accountServiceMock.findById(account.getId())).thenReturn(account);
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        MvcResult mvcResult = this.mvc.perform(post(API_LIST_URL + list.getId() + ARCHIVE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(account.getId()))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getShared().contains(account.getId())).isFalse();
        assertThat(result.isArchived()).isFalse();
    }

    @Test
    public void deleteSharedList() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        Account account = TestUtil.createAccount(JOHN, DOE);
        account.setId(TestUtil.ADMIN_RANDOM_ID);
        list.setOwnerId(account.getId());
        list.getShared().add(testAccount.getId());
        when(listRepositoryMock.findById(list.getId())).thenReturn(Optional.of(list));
        when(accountServiceMock.findById(account.getId())).thenReturn(account);
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        this.mvc.perform(post(API_LIST_URL + list.getId() + DELETE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(account.getId()))
                .andExpect(status().is2xxSuccessful()).andReturn();
        verify(listRepositoryMock, times(1)).save(list);
    }

    @Test
    public void addItemTest() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        ShoppingList list = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        when(productRepositoryMock.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(productRepositoryMock.save(any())).then(returnsFirstArg());
        when(listItemRepositoryMock.save(any())).then(returnsFirstArg());
        MvcResult mvcResult = this.mvc.perform(post(API_LIST_URL + list.getId() + ITEM_ADD)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem))).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getItems().size() == 1).isTrue();
        assertThat(result.getItems().contains(listItem)).isTrue();
    }

    @Test
    public void addItemBadProductName() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        listItem.getProduct().setName(null);
        ShoppingList list = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        this.mvc.perform(post(API_LIST_URL + list.getId() + ITEM_ADD)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addItemProductNotFound() throws Exception, BadProductNameException {
        ListItem listItem = TestUtil.createListItem(NAME);
        listItem.getProduct().setId(1L);
        ShoppingList list = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(productRepositoryMock.findById(anyLong())).thenReturn(Optional.empty());
        this.mvc.perform(post(API_LIST_URL + list.getId() + ITEM_ADD)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addItemListNotFound() throws Exception, BadProductNameException {
        ListItem listItem = TestUtil.createListItem(NAME);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        this.mvc.perform(post(API_LIST_URL + 1 + ITEM_ADD)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addItemListNoPermission() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        ShoppingList list1 = createList(NAME, 1L);
        list1.setOwnerId(NAME);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list1));
        this.mvc.perform(post(API_LIST_URL + 1 + ITEM_ADD)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isForbidden());
    }


    private ShoppingList createList(String name, long id) {
        return TestUtil.createShoppingList(name, id, testAccount);
    }


}
package com.qprogramming.shopper.app.api.shoppinglist;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.config.mail.MailService;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.items.ListItem;
import com.qprogramming.shopper.app.items.ListItemRepository;
import com.qprogramming.shopper.app.items.ListItemService;
import com.qprogramming.shopper.app.items.category.Category;
import com.qprogramming.shopper.app.items.favorites.FavoriteProductsRepository;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import com.qprogramming.shopper.app.messages.MessagesService;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListRepository;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPreset;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPresetRepository;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPresetService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.cache.CacheManager;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
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
    private static final String EDIT = "/edit";
    private static final String ITEM_CLEANUP = "/cleanup";
    private static final String MINE = "mine";
    private static final String USER = "user/";
    private static final String PRESETS = "presets";
    private static final String PRESETS_UPDATE = "presets/update";
    private static final String PRESETS_DELETE = "presets/delete";

    @Mock
    private ShoppingListRepository listRepositoryMock;
    @Mock
    private AccountService accountServiceMock;
    @Mock
    private PropertyService propertyServiceMock;
    @Mock
    private MailService mailServiceMock;
    @Mock
    private ListItemRepository listItemRepositoryMock;
    @Mock
    private ProductRepository productRepositoryMock;
    @Mock
    private CategoryPresetRepository presetRepositoryMock;
    @Mock
    private FavoriteProductsRepository favoritesRepositoryMock;
    @Mock
    private MessagesService msgSrvMock;
    @Mock
    private CacheManager cacheManager;



    @Before
    @Override
    public void setup() {
        super.setup();
        CategoryPresetService presetService = new CategoryPresetService(presetRepositoryMock);
        ShoppingListService listService = new ShoppingListService(listRepositoryMock, accountServiceMock, propertyServiceMock, msgSrvMock, mailServiceMock, presetService);
        ListItemService listItemService = new ListItemService(listItemRepositoryMock, productRepositoryMock, favoritesRepositoryMock,cacheManager);
        ShoppingListRestController controller = new ShoppingListRestController(listService, listItemService, presetService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }

    @Test
    @WithUserDetails(value = TestUtil.EMAIL, userDetailsServiceBeanName = "accountService")
    public void getCurrentUserListsTest() throws Exception {
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
    public void getCurrentUserListsWithSharedTest() throws Exception {
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
    public void getCurrentUserListsWithSharedNoOwnerInDBTest() throws Exception {
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
    public void getUserListsNotFoundTest() throws Exception {
        when(accountServiceMock.findById(testAccount.getId())).thenThrow(new AccountNotFoundException());
        this.mvc.perform(get(API_LIST_URL + USER + testAccount.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getUserListsWithSharedForCurrentUserTest() throws Exception {
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
    public void addNewListTest() throws Exception {
        ShoppingList newList = new ShoppingList();
        newList.setName(NAME);
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        MvcResult mvcResult = this.mvc.perform(post(API_LIST_URL + "add")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(newList)))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getOwnerId()).isEqualTo(TestUtil.USER_RANDOM_ID);
    }

    @Test
    public void getListTest() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        MvcResult mvcResult = this.mvc.perform(get(API_LIST_URL + 1))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getOwnerId()).isEqualTo(TestUtil.USER_RANDOM_ID);
    }

    @Test
    public void getListNoPermissionTest() throws Exception {
        ShoppingList list1 = createList(NAME, 1L);
        list1.setOwnerId(NAME);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list1));
        this.mvc.perform(get(API_LIST_URL + 1))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shareListTest() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        Account account = TestUtil.createAccount(JOHN, DOE);
        account.setId(TestUtil.ADMIN_RANDOM_ID);
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        when(listRepositoryMock.findById(list.getId())).thenReturn(Optional.of(list));
        when(accountServiceMock.findByEmail(account.getEmail())).thenReturn(Optional.of(account));
        MvcResult mvcResult = this.mvc.perform(post(API_LIST_URL + list.getId() + SHARE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(account.getEmail()))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getShared().contains(account.getId())).isTrue();
    }

    @Test
    public void shareListNotFoundTest() throws Exception {
        this.mvc.perform(post(API_LIST_URL + 1L + SHARE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shareListAccountNotFoundTest() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        when(listRepositoryMock.findById(list.getId())).thenReturn(Optional.of(list));
        when(accountServiceMock.findByEmail(TestUtil.ADMIN_RANDOM_ID)).thenReturn(Optional.empty());
        MvcResult mvcResult = this.mvc.perform(post(API_LIST_URL + 1L + SHARE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getShared().contains(TestUtil.ADMIN_RANDOM_ID)).isFalse();
    }

    @Test
    public void stopSharingListTest() throws Exception {
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
    public void listOperationsWithoutPermissionTest() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        Account account = TestUtil.createAccount(JOHN, DOE);
        account.setId(TestUtil.ADMIN_RANDOM_ID);
        list.setOwnerId(account.getId());
        when(listRepositoryMock.findById(list.getId())).thenReturn(Optional.of(list));
        this.mvc.perform(post(API_LIST_URL + 1L + SHARE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().isForbidden());
        this.mvc.perform(post(API_LIST_URL + 1L + STOP_SHARING).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().isForbidden());
        this.mvc.perform(post(API_LIST_URL + 1L + DELETE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().isForbidden());
        this.mvc.perform(post(API_LIST_URL + 1L + ARCHIVE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().isForbidden());
        this.mvc.perform(post(API_LIST_URL + 1L + ITEM_CLEANUP)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)).andExpect(status().isForbidden());


    }

    @Test
    public void listOperationListNotFoundTest() throws Exception {
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        this.mvc.perform(get(API_LIST_URL + 1))
                .andExpect(status().isNotFound());
        this.mvc.perform(post(API_LIST_URL + 1L + ARCHIVE))
                .andExpect(status().isNotFound());
        this.mvc.perform(post(API_LIST_URL + 1L + ITEM_CLEANUP))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteListNotFoundTest() throws Exception {
        this.mvc.perform(post(API_LIST_URL + 1L + DELETE).contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.ADMIN_RANDOM_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void archiveListTest() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        MvcResult mvcResult = this.mvc.perform(post(API_LIST_URL + 1 + ARCHIVE))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.isArchived()).isTrue();
    }

    @Test
    public void deleteListTest() throws Exception {
        ShoppingList list1 = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list1));
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        this.mvc.perform(post(API_LIST_URL + 1 + DELETE))
                .andExpect(status().is2xxSuccessful());
        verify(listRepositoryMock, times(1)).delete(list1);
    }

    @Test
    public void archiveSharedListTest() throws Exception {
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
    public void deleteSharedListTest() throws Exception {
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
    public void getListSortedTest() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        ListItem item1 = TestUtil.createListItem(NAME);
        ListItem item2 = TestUtil.createListItem(NAME);
        ListItem item3 = TestUtil.createListItem(NAME);
        item1.setCategory(Category.OTHER);
        item2.setCategory(Category.ALCOHOL);
        item3.setCategory(Category.FRUIT_VEGETABLES);
        list.getItems().add(item1);
        list.getItems().add(item2);
        list.getItems().add(item3);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(propertyServiceMock.getCategoriesOrdered()).thenCallRealMethod();
        MvcResult mvcResult = this.mvc.perform(get(API_LIST_URL + 1))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getItems().get(0)).isEqualTo(item2);
    }

    @Test
    public void editListTestNoPermission() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        list.setOwnerId(TestUtil.ADMIN_RANDOM_ID);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        String new_name = "NEW NAME";
        this.mvc.perform(post(API_LIST_URL + EDIT)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(list)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void editListTestNotFound() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        this.mvc.perform(post(API_LIST_URL + EDIT)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(list)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void editListEmptyName() throws Exception {
        String new_name = "";
        this.mvc.perform(post(API_LIST_URL + EDIT).content(new_name))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void editListTest() throws Exception {
        String new_name = "NEW NAME";
        ShoppingList list = createList(NAME, 1L);
        ShoppingList updatedlist = createList(new_name, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        MvcResult mvcResult = this.mvc.perform(post(API_LIST_URL + EDIT)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedlist)))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getName()).isEqualTo(new_name);
    }

    @Test
    public void cleanupListTest() throws Exception {
        ShoppingList list = createList(NAME, 1L);
        ListItem item1 = TestUtil.createListItem(NAME);
        ListItem item2 = TestUtil.createListItem(NAME);
        ListItem item3 = TestUtil.createListItem(NAME);
        item1.setId(1L);
        item2.setId(2L);
        item3.setId(3L);
        item2.setDone(true);
        item3.setDone(true);
        list.getItems().add(item1);
        list.getItems().add(item2);
        list.getItems().add(item3);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        when(propertyServiceMock.getCategoriesOrdered()).thenCallRealMethod();
        MvcResult mvcResult = this.mvc.perform(post(API_LIST_URL + 1 + ITEM_CLEANUP)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getItems()).containsOnly(item1);
        verify(listItemRepositoryMock, times(2)).delete(any(ListItem.class));
    }


    @Test
    public void createPresetTest() throws Exception {
        CategoryPreset preset = new CategoryPreset();
        when(presetRepositoryMock.save(any(CategoryPreset.class))).then(returnsFirstArg());
        MvcResult mvcResult = mvc.perform(post(API_LIST_URL + PRESETS_UPDATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(preset)))
                .andExpect(status().isOk()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        CategoryPreset result = TestUtil.convertJsonToObject(jsonResponse, CategoryPreset.class);
        assertThat(result.getOwnerId()).isEqualTo(testAccount.getId());
        verify(presetRepositoryMock, times(1)).save(any(CategoryPreset.class));
    }

    @Test
    public void updatePresetTest() throws Exception {
        CategoryPreset preset = new CategoryPreset();
        preset.setId(1L);
        preset.setOwnerId(testAccount.getId());
        preset.setName(NAME);
        CategoryPreset dbpreset = new CategoryPreset();
        dbpreset.setId(1L);
        dbpreset.setOwnerId(testAccount.getId());
        when(presetRepositoryMock.findById(1L)).thenReturn(Optional.of(dbpreset));
        when(presetRepositoryMock.save(any(CategoryPreset.class))).then(returnsFirstArg());
        MvcResult mvcResult = mvc.perform(post(API_LIST_URL + PRESETS_UPDATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(preset)))
                .andExpect(status().isOk()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        CategoryPreset result = TestUtil.convertJsonToObject(jsonResponse, CategoryPreset.class);
        assertThat(result.getOwnerId()).isEqualTo(testAccount.getId());
        assertThat(result.getName()).isEqualTo(NAME);
        verify(presetRepositoryMock, times(1)).save(any(CategoryPreset.class));
    }


    @Test
    public void getUserPresetsTest() throws Exception {

        when(presetRepositoryMock.findAllByOwnerIdOrOwnersIn(testAccount.getId(), Collections.singleton(testAccount.getId()))).thenReturn(Collections.singleton(new CategoryPreset()));
        MvcResult mvcResult = mvc.perform(get(API_LIST_URL + PRESETS)).andExpect(status().isOk()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<CategoryPreset> result = TestUtil.convertJsonToList(jsonResponse, List.class, CategoryPreset.class);
        assertThat(result.size()).isEqualTo(1);

    }

    @Test
    public void presetNotFoundOperationsTest() throws Exception {
        CategoryPreset preset = new CategoryPreset();
        preset.setId(1L);
        when(presetRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        mvc.perform(post(API_LIST_URL + PRESETS_DELETE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(preset)))
                .andExpect(status().isNotFound());
        mvc.perform(post(API_LIST_URL + PRESETS_UPDATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(preset)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void presetNotOwnerOperationsTest() throws Exception {
        CategoryPreset preset = new CategoryPreset();
        preset.setId(1L);
        preset.setOwnerId(TestUtil.ADMIN_RANDOM_ID);
        when(presetRepositoryMock.findById(1L)).thenReturn(Optional.of(preset));
        mvc.perform(post(API_LIST_URL + PRESETS_DELETE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(preset)))
                .andExpect(status().isForbidden());
        mvc.perform(post(API_LIST_URL + PRESETS_UPDATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(preset)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deletePresetTest() throws Exception {
        CategoryPreset preset = new CategoryPreset();
        preset.setId(1L);
        preset.setOwnerId(testAccount.getId());
        ShoppingList shoppingList1 = TestUtil.createShoppingList(NAME, 1L, testAccount);
        ShoppingList shoppingList2 = TestUtil.createShoppingList(NAME, 2L, testAccount);
        shoppingList1.setPreset(preset);
        shoppingList2.setPreset(preset);
        List<ShoppingList> shoppingLists = Arrays.asList(shoppingList1, shoppingList2);
        when(presetRepositoryMock.findById(1L)).thenReturn(Optional.of(preset));
        when(listRepositoryMock.findAllByPreset(preset)).thenReturn(shoppingLists);
        mvc.perform(post(API_LIST_URL + PRESETS_DELETE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(preset)))
                .andExpect(status().isOk());
        verify(presetRepositoryMock, times(1)).delete(any(CategoryPreset.class));
        verify(listRepositoryMock, times(1)).saveAll(shoppingLists);
    }


    private ShoppingList createList(String name, long id) {
        return TestUtil.createShoppingList(name, id, testAccount);
    }


}
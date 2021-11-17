package com.qprogramming.shopper.app.api.items;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.config.mail.MailService;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.items.ListItem;
import com.qprogramming.shopper.app.items.ListItemRepository;
import com.qprogramming.shopper.app.items.ListItemService;
import com.qprogramming.shopper.app.items.category.Category;
import com.qprogramming.shopper.app.items.favorites.FavoriteProducts;
import com.qprogramming.shopper.app.items.favorites.FavoriteProductsRepository;
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import com.qprogramming.shopper.app.messages.MessagesService;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListRepository;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPresetRepository;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPresetService;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Jakub Romaniszyn on 2018-08-16
 */
public class ItemRestControllerTest extends MockedAccountTestBase {

    private static final String NAME = "name";
    private static final String API_ITEM_URL = "/api/item/";
    private static final String ITEM_ADD = "/add";
    private static final String ITEM_UPDATE = "/update";
    private static final String ITEM_DELETE = "/delete";
    private static final String ITEM_TOGGLE = "/toggle";
    private static final String FAVORITES_LIST = "favorites/list/";

    @Mock
    private ShoppingListRepository listRepositoryMock;
    @Mock
    private AccountService accountServiceMock;
    @Mock
    private ProductRepository productRepositoryMock;
    @Mock
    private ListItemRepository listItemRepositoryMock;
    @Mock
    private PropertyService propertyServiceMock;
    @Mock
    private MailService mailServiceMock;
    @Mock
    private CategoryPresetRepository presetRepositoryMock;
    @Mock
    private FavoriteProductsRepository favoritesRepositoryMock;
    @Mock
    private MessagesService msgSrvMock;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cacheMock;


    @BeforeEach
    void setUp() {
        super.setup();
        when(cacheManager.getCache(anyString())).thenReturn(cacheMock);
        val presetService = new CategoryPresetService(presetRepositoryMock);
        val listService = new ShoppingListService(listRepositoryMock, accountServiceMock, propertyServiceMock, msgSrvMock, mailServiceMock, presetService);
        val listItemService = new ListItemService(listItemRepositoryMock, productRepositoryMock, favoritesRepositoryMock, cacheManager);
        val controller = new ItemRestController(listItemService, listService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }


    @Test
    void addItemTest() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        ShoppingList list = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        when(productRepositoryMock.findByNameIgnoreCaseAndLanguage(anyString(), anyString())).thenReturn(Optional.empty());
        when(productRepositoryMock.save(any())).then(returnsFirstArg());
        when(listItemRepositoryMock.save(any())).then(returnsFirstArg());
        MvcResult mvcResult = this.mvc.perform(post(API_ITEM_URL + list.getId() + ITEM_ADD)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem))).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getItems().size() == 1).isTrue();
        verify(listItemRepositoryMock, times(1)).save(any(ListItem.class));
        verify(listRepositoryMock, times(1)).save(any(ShoppingList.class));
        verify(cacheMock, times(1)).evict(testAccount.getId());
    }

    @Test
    void addItemAlreadyExistsTest() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        ShoppingList list = createList(NAME, 1L);
        list.getItems().add(listItem);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        when(productRepositoryMock.findByNameIgnoreCaseAndLanguage(anyString(), anyString())).thenReturn(Optional.empty());
        when(productRepositoryMock.save(any())).then(returnsFirstArg());
        when(listItemRepositoryMock.save(any())).then(returnsFirstArg());
        MvcResult mvcResult = this.mvc.perform(post(API_ITEM_URL + list.getId() + ITEM_ADD)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem))).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getItems().size() == 1).isTrue();
        assertThat(result.getItems().get(0).getQuantity() == 2f).isTrue();
        verify(listRepositoryMock, times(1)).save(any(ShoppingList.class));
    }

    @Test
    void addItemAlreadyExistsAndIsDoneTest() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        listItem.setDone(true);
        ShoppingList list = createList(NAME, 1L);
        list.getItems().add(listItem);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        when(productRepositoryMock.findByNameIgnoreCaseAndLanguage(anyString(), anyString())).thenReturn(Optional.empty());
        when(productRepositoryMock.save(any())).then(returnsFirstArg());
        when(listItemRepositoryMock.save(any())).then(returnsFirstArg());
        MvcResult mvcResult = this.mvc.perform(post(API_ITEM_URL + list.getId() + ITEM_ADD)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem))).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getItems().size() == 1).isTrue();
        assertThat(result.getItems().get(0).getQuantity() == 1f).isTrue();
        assertThat(result.getItems().get(0).isDone()).isFalse();
        verify(listRepositoryMock, times(1)).save(any(ShoppingList.class));
    }

    @Test
    void addItemBadProductNameTest() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        listItem.getProduct().setName(null);
        ShoppingList list = createList(NAME, 1L);

        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        this.mvc.perform(post(API_ITEM_URL + list.getId() + ITEM_ADD)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemOperationsProductNotFoundTest() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        listItem.getProduct().setId(1L);
        ShoppingList list = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(productRepositoryMock.findById(anyLong())).thenReturn(Optional.empty());

        this.mvc.perform(post(API_ITEM_URL + list.getId() + ITEM_ADD)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isNotFound());
    }

    @Test
    void itemOperationsListNotFoundTest() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.empty());

        this.mvc.perform(post(API_ITEM_URL + 1 + ITEM_ADD)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isNotFound());
        this.mvc.perform(post(API_ITEM_URL + 1 + ITEM_UPDATE)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isNotFound());
        this.mvc.perform(post(API_ITEM_URL + 1 + ITEM_DELETE)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isNotFound());
        this.mvc.perform(post(API_ITEM_URL + 1 + ITEM_TOGGLE)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isNotFound());
        this.mvc.perform(get(API_ITEM_URL + 1 + FAVORITES_LIST))
                .andExpect(status().isNotFound());
    }

    @Test
    void itemOperationsListNoPermissionTest() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        ShoppingList list1 = createList(NAME, 1L);
        list1.setOwnerId(NAME);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list1));

        this.mvc.perform(post(API_ITEM_URL + 1 + ITEM_ADD)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isForbidden());
        this.mvc.perform(post(API_ITEM_URL + 1 + ITEM_UPDATE)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isForbidden());
        this.mvc.perform(post(API_ITEM_URL + 1 + ITEM_DELETE)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isForbidden());
        this.mvc.perform(post(API_ITEM_URL + 1 + ITEM_TOGGLE)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(listItem)))
                .andExpect(status().isForbidden());
        this.mvc.perform(get(API_ITEM_URL + FAVORITES_LIST + 1))
                .andExpect(status().isForbidden());

    }


    @Test
    void updateItemTest() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        ListItem dbItem = TestUtil.createListItem(NAME);
        listItem.setId(1L);
        listItem.setCategory(Category.ALCOHOL);
        dbItem.setCategory(Category.OTHER);
        dbItem.setId(1L);
        ShoppingList list = createList(NAME, 1L);
        list.setItems(Collections.singletonList(dbItem));
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(listItemRepositoryMock.findById(1L)).thenReturn(Optional.of(dbItem));
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.of(dbItem.getProduct()));
        when(productRepositoryMock.save(any())).then(returnsFirstArg());
        when(listItemRepositoryMock.save(any())).then(returnsFirstArg());
        when(listRepositoryMock.save(any())).then(returnsFirstArg());
        MvcResult mvcResult = this.mvc.perform(post(API_ITEM_URL + list.getId() + ITEM_UPDATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem))).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        verify(productRepositoryMock, times(1)).save(dbItem.getProduct());
        verify(listRepositoryMock, times(1)).save(any(ShoppingList.class));
    }

    @Test
    void updateItemProductChangedTest() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        listItem.setProduct(TestUtil.createProduct("New Product"));
        ListItem dbItem = TestUtil.createListItem(NAME);
        dbItem.setId(1L);
        dbItem.setCategory(Category.ALCOHOL);
        listItem.setId(1L);
        listItem.setCategory(Category.ALCOHOL);
        ShoppingList list = createList(NAME, 1L);
        list.getItems().add(dbItem);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(listItemRepositoryMock.findById(1L)).thenReturn(Optional.of(dbItem));
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.of(dbItem.getProduct()));
        when(productRepositoryMock.save(any())).then(returnsFirstArg());
        when(listItemRepositoryMock.save(any())).then(returnsFirstArg());
        this.mvc.perform(post(API_ITEM_URL + list.getId() + ITEM_UPDATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem))).andExpect(status().is2xxSuccessful());
        verify(productRepositoryMock, times(2)).save(any(Product.class));
        verify(listRepositoryMock, times(1)).save(any(ShoppingList.class));
    }


    @Test
    void itemOperationsItemNotFoundTest() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        listItem.setId(1L);
        ShoppingList list = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(listItemRepositoryMock.findById(1L)).thenReturn(Optional.empty());

        this.mvc.perform(post(API_ITEM_URL + list.getId() + ITEM_UPDATE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem))).andExpect(status().isNotFound());
        this.mvc.perform(post(API_ITEM_URL + list.getId() + ITEM_DELETE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem))).andExpect(status().isNotFound());
        this.mvc.perform(post(API_ITEM_URL + list.getId() + ITEM_TOGGLE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem))).andExpect(status().isNotFound());
    }


    @Test
    void deleteItemTest() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        listItem.setId(1L);
        ShoppingList list = createList(NAME, 1L);
        list.getItems().add(listItem);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(listItemRepositoryMock.findById(1L)).thenReturn(Optional.of(listItem));
        when(listRepositoryMock.save(any())).then(returnsFirstArg());

        MvcResult mvcResult = this.mvc.perform(post(API_ITEM_URL + list.getId() + ITEM_DELETE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem))).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList shoppingList = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        verify(listItemRepositoryMock, times(1)).delete(listItem);
        assertThat(shoppingList.getItems().isEmpty()).isTrue();
    }

    @Test
    void toggleItemTest() throws Exception {
        ListItem listItem = TestUtil.createListItem(NAME);
        listItem.setId(1L);
        ShoppingList list = createList(NAME, 1L);
        list.getItems().add(listItem);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(listItemRepositoryMock.findById(1L)).thenReturn(Optional.of(listItem));
        when(listItemRepositoryMock.save(any())).then(returnsFirstArg());

        MvcResult mvcResult = this.mvc.perform(post(API_ITEM_URL + list.getId() + ITEM_TOGGLE)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem))).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ListItem result = TestUtil.convertJsonToObject(contentAsString, ListItem.class);
        verify(listItemRepositoryMock, times(1)).save(listItem);
        assertThat(result.isDone()).isTrue();
        this.mvc.perform(get("/api/item/" + list.getId() + ITEM_TOGGLE + "/" + listItem.getId()))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void getFavoritesNothingFoundTest() throws Exception {
        ListItem listItem = new ListItem();
        listItem.setId(1L);
        ShoppingList list = createList(NAME, 1L);
        list.getItems().add(listItem);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(favoritesRepositoryMock.findById(testAccount.getId())).thenReturn(Optional.empty());
        MvcResult mvcResult = this.mvc.perform(get(API_ITEM_URL + FAVORITES_LIST + list.getId()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Product> favorites = TestUtil.convertJsonToList(contentAsString, List.class, Product.class);
        assertThat(favorites).isEmpty();

    }

    @Test
    void favoritesTest() throws Exception {
        Product product1 = TestUtil.createProduct(NAME + 1);
        Product product2 = TestUtil.createProduct(NAME + 2);
        Product product3 = TestUtil.createProduct(NAME + 3);
        FavoriteProducts fav = new FavoriteProducts();
        fav.getFavorites().put(product1, 1L);
        fav.getFavorites().put(product2, 2L);
        fav.getFavorites().put(product3, 3L);
        ListItem listItem = new ListItem();
        listItem.setProduct(product1);
        listItem.setId(1L);
        ShoppingList list = createList(NAME, 1L);
        list.getItems().add(listItem);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list));
        when(favoritesRepositoryMock.findById(testAccount.getId())).thenReturn(Optional.of(fav));
        MvcResult mvcResult = this.mvc.perform(get(API_ITEM_URL + FAVORITES_LIST + list.getId()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Product> favorites = TestUtil.convertJsonToList(contentAsString, List.class, Product.class);
        assertThat(favorites.size()).isEqualTo(2);
        assertThat(favorites.get(0)).isEqualTo(product3);
    }


    private ShoppingList createList(String name, long id) {
        return TestUtil.createShoppingList(name, id, testAccount);
    }


}

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
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListRepository;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPresetRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
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

    private ListItemService listItemService;

    private ShoppingListService listService;
    private ItemRestController controller;

    @Before
    @Override
    public void setup() {
        super.setup();
        listService = new ShoppingListService(listRepositoryMock, accountServiceMock, propertyServiceMock, mailServiceMock, presetRepositoryMock);
        listItemService = new ListItemService(listItemRepositoryMock, productRepositoryMock);
        controller = new ItemRestController(listItemService, listService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
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
        MvcResult mvcResult = this.mvc.perform(post(API_ITEM_URL + list.getId() + ITEM_ADD)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(listItem))).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList result = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(result.getItems().size() == 1).isTrue();
        verify(listItemRepositoryMock, times(1)).save(any(ListItem.class));
        verify(listRepositoryMock, times(1)).save(any(ShoppingList.class));
    }

    @Test
    public void addItemBadProductNameTest() throws Exception {
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
    public void itemOperationsProductNotFoundTest() throws Exception {
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
    public void itemOperationsListNotFoundTest() throws Exception {
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

    }

    @Test
    public void itemOperationsListNoPermissionTest() throws Exception {
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
    }


    @Test
    public void updateItemTest() throws Exception {
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
    public void updateItemProductChangedTest() throws Exception {
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
    public void itemOperationsItemNotFoundTest() throws Exception {
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
    public void deleteItemTest() throws Exception {
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
    public void toggleItemTest() throws Exception {
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
    }

    private ShoppingList createList(String name, long id) {
        return TestUtil.createShoppingList(name, id, testAccount);
    }


}
package com.qprogramming.shopper.app.api.shoppinglist;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListRepository;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
public class ShoppingListRestControllerAccountTest extends MockedAccountTestBase {

    public static final String NAME = "name";

    @Mock
    private ShoppingListRepository listRepositoryMock;

    private ShoppingListService listService;
    private ShoppingListRestController controller;

    @Before
    @Override
    public void setup() {
        super.setup();
        listService = new ShoppingListService(listRepositoryMock);
        controller = new ShoppingListRestController(listService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }

    @Test
    @WithUserDetails(value = TestUtil.EMAIL, userDetailsServiceBeanName = "accountService")
    public void getCurrentUserLists() throws Exception {
        ShoppingList list1 = createList(NAME, 1L);
        ShoppingList list2 = createList(NAME, 2L);
        List<ShoppingList> shoppingList = Arrays.asList(list1, list2);
        when(listRepositoryMock.findAllByOwnerId(TestUtil.USER_RANDOM_ID)).thenReturn(shoppingList);
        MvcResult mvcResult = this.mvc.perform(get("/api/list/mine"))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<ShoppingList> result = TestUtil.convertJsonToList(jsonResponse, List.class, ShoppingList.class);
        assertThat(result.containsAll(shoppingList)).isTrue();
    }

    @Test
    public void addNewList() throws Exception {
        when(listRepositoryMock.save(any(ShoppingList.class))).then(returnsFirstArg());
        MvcResult mvcResult = this.mvc.perform(post("/api/list/add").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(NAME))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList shoppingList = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(shoppingList.getOwnerId()).isEqualTo(TestUtil.USER_RANDOM_ID);
    }

    @Test
    public void getList() throws Exception {
        ShoppingList list1 = createList(NAME, 1L);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list1));
        MvcResult mvcResult = this.mvc.perform(get("/api/list/1"))
                .andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ShoppingList shoppingList = TestUtil.convertJsonToObject(contentAsString, ShoppingList.class);
        assertThat(shoppingList.getOwnerId()).isEqualTo(TestUtil.USER_RANDOM_ID);
    }

    @Test
    public void getListNotFound() throws Exception {
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        this.mvc.perform(get("/api/list/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getListNoPermission() throws Exception {
        ShoppingList list1 = createList(NAME, 1L);
        list1.setOwnerId(NAME);
        when(listRepositoryMock.findById(1L)).thenReturn(Optional.of(list1));
        this.mvc.perform(get("/api/list/1"))
                .andExpect(status().is4xxClientError());
    }

    private ShoppingList createList(String name, long id) {
        return TestUtil.createShoppingList(name, id, testAccount);
    }


}
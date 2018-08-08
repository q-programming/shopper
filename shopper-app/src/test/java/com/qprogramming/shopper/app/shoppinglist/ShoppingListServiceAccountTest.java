package com.qprogramming.shopper.app.shoppinglist;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
public class ShoppingListServiceAccountTest extends MockedAccountTestBase {
    public static final String NAME = "name";
    @Mock
    private ShoppingListRepository listRepository;

    private ShoppingListService listService;


    @Before
    @Override
    public void setup() {
        super.setup();
        listService = new ShoppingListService(listRepository);
    }

    @Test
    public void findAllByCurrentUser() {
        ShoppingList list1 = createList(NAME, 1L);
        ShoppingList list2 = createList(NAME, 2L);
        List<ShoppingList> expected = Arrays.asList(list1, list2);
        when(listRepository.findAllByOwnerId(testAccount.getId())).thenReturn(expected);
        List<ShoppingList> result = listService.findAllByCurrentUser();
        assertThat(result.containsAll(expected)).isTrue();
    }

    @Test
    public void findAllByOwner() {
        ShoppingList list1 = createList(NAME, 1L);
        List<ShoppingList> expected = Collections.singletonList(list1);
        when(listRepository.findAllByOwnerId(testAccount.getId())).thenReturn(expected);
        List<ShoppingList> result = listService.findAllByOwnerID(testAccount.getId());
        assertThat(result.containsAll(expected)).isTrue();
    }

    @Test
    public void findAllByOwnerThanCanBeViewed() {
        ShoppingList list1 = createList(NAME, 1L);
        ShoppingList list2 = createList(NAME, 1L);
        list2.setOwnerId(NAME);
        when(listRepository.findAllByOwnerId(testAccount.getId())).thenReturn(Arrays.asList(list1, list2));
        List<ShoppingList> result = listService.findAllByOwnerID(testAccount.getId());
        assertThat(result.contains(list2)).isFalse();
    }


    private ShoppingList createList(String name, long id) {
        return TestUtil.createShoppingList(name, id, testAccount);
    }
}
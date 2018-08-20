package com.qprogramming.shopper.app.shoppinglist;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
public class ShoppingListServiceTest extends MockedAccountTestBase {
    public static final String NAME = "name";
    @Mock
    private ShoppingListRepository listRepository;
    @Mock
    private AccountService accountServiceMock;
    @Mock
    private PropertyService propertyServiceMock;

    private ShoppingListService listService;


    @Before
    @Override
    public void setup() {
        super.setup();
        listService = new ShoppingListService(listRepository, accountServiceMock, propertyServiceMock);
    }

    @Test
    public void findAllByCurrentUserTest() throws AccountNotFoundException {
        ShoppingList list1 = createList(NAME, 1L);
        ShoppingList list2 = createList(NAME, 2L);
        Set<ShoppingList> expected = Stream.of(list1, list2).collect(Collectors.toSet());
        when(listRepository.findAllByOwnerIdOrSharedIn(anyString(), anySet())).thenReturn(expected);
        when(accountServiceMock.findById(testAccount.getId())).thenReturn(testAccount);
        Set<ShoppingList> result = listService.findAllByCurrentUser(false);
        assertThat(result.containsAll(expected)).isTrue();
    }

    @Test
    public void findAllByOwnerTest() throws AccountNotFoundException {
        ShoppingList list1 = createList(NAME, 1L);
        Set<ShoppingList> expected = Stream.of(list1).collect(Collectors.toSet());
        when(listRepository.findAllByOwnerIdOrSharedIn(anyString(), anySet())).thenReturn(expected);
        when(accountServiceMock.findById(testAccount.getId())).thenReturn(testAccount);
        Set<ShoppingList> result = listService.findAllByAccountID(testAccount.getId(), false);
        assertThat(result.containsAll(expected)).isTrue();
    }

    @Test
    public void findAllByOwnerThanCanBeViewedTest() throws AccountNotFoundException {
        ShoppingList list1 = createList(NAME, 1L);
        ShoppingList list2 = createList(NAME, 1L);
        list2.setOwnerId(NAME);
        when(listRepository.findAllByOwnerId(testAccount.getId())).thenReturn(Arrays.asList(list1, list2));
        when(accountServiceMock.findById(testAccount.getId())).thenReturn(testAccount);
        Set<ShoppingList> result = listService.findAllByAccountID(testAccount.getId(), false);
        assertThat(result.contains(list2)).isFalse();
    }


    private ShoppingList createList(String name, long id) {
        return TestUtil.createShoppingList(name, id, testAccount);
    }
}
package com.qprogramming.shopper.app.shoppinglist;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.config.mail.MailService;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPresetRepository;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPresetService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    @Mock
    private MailService mailServiceMock;
    @Mock
    private CategoryPresetRepository presetRepositoryMock;

    private ShoppingListService listService;


    @Before
    @Override
    public void setup() {
        super.setup();
        CategoryPresetService presetService = new CategoryPresetService(presetRepositoryMock);
        listService = new ShoppingListService(listRepository, accountServiceMock, propertyServiceMock, mailServiceMock, presetService);
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

    @Test
    public void addToListIfPending() {
        ShoppingList list1 = createList(NAME, 1L);
        ShoppingList list2 = createList(NAME, 2L);
        list1.getPendingshares().add(testAccount.getEmail());
        list2.getPendingshares().add(testAccount.getEmail());
        List<ShoppingList> shoppingLists = Arrays.asList(list1, list2);
        when(listRepository.findAllByPendingshares(testAccount.getEmail())).thenReturn(shoppingLists);
        listService.addToListIfPending(testAccount);
        verify(listRepository, times(1)).saveAll(shoppingLists);

    }


    private ShoppingList createList(String name, long id) {
        return TestUtil.createShoppingList(name, id, testAccount);
    }
}
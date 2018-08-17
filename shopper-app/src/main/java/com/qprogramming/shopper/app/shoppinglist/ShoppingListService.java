package com.qprogramming.shopper.app.shoppinglist;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.ShoppingAccessException;
import com.qprogramming.shopper.app.exceptions.ShoppingNotFoundException;
import com.qprogramming.shopper.app.items.ListItem;
import com.qprogramming.shopper.app.items.category.Category;
import com.qprogramming.shopper.app.support.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
@Service
public class ShoppingListService {

    private ShoppingListRepository _listRepository;
    private AccountService _accountService;
    private PropertyService _propertyService;


    @Autowired
    public ShoppingListService(ShoppingListRepository listRepository, AccountService accountService, PropertyService propertyService) {
        this._listRepository = listRepository;
        this._accountService = accountService;
        _propertyService = propertyService;
    }

    public Set<ShoppingList> findAllByCurrentUser(boolean archived) throws AccountNotFoundException {
        return findAllByAccountID(Utils.getCurrentAccountId(), archived);
    }

    public Set<ShoppingList> findAllByAccountID(String accountId, boolean archived) throws AccountNotFoundException {
        Account account = this._accountService.findById(accountId);
        Set<ShoppingList> list = this._listRepository.findAllByOwnerIdOrSharedIn(account.getId(), Collections.singleton(account.getId()));
        return list.stream().filter(shoppingList -> shoppingList.isArchived() == archived).collect(Collectors.toSet());
    }

    public boolean canView(ShoppingList list) {
        HashSet<String> accounts = new HashSet<>(list.getShared());
        accounts.add(list.getOwnerId());
        return accounts.stream().anyMatch(Predicate.isEqual(Utils.getCurrentAccountId()));
    }

    public ShoppingList addList(String name) {
        Account currentAccount = Utils.getCurrentAccount();
        ShoppingList list = new ShoppingList();
        list.setName(name);
        list.setOwnerId(currentAccount.getId());
        list.setOwnerName(currentAccount.getName());
        return this.save(list);
    }

    /**
     * returns list by id
     *
     * @param id id of lists to be returned
     * @return Shopping list
     * @throws ShoppingAccessException if currently logged in user don't have access to list
     */
    public ShoppingList findByID(String id) throws ShoppingAccessException, ShoppingNotFoundException {
        Optional<ShoppingList> listOptional = _listRepository.findById(Long.valueOf(id));
        if (listOptional.isPresent()) {
            ShoppingList shoppingList = listOptional.get();
            if (!canView(shoppingList)) {
                throw new ShoppingAccessException();
            }
            return shoppingList;
        }
        throw new ShoppingNotFoundException();
    }


    public ShoppingList shareList(ShoppingList list, String accountID) throws AccountNotFoundException {
        Account account = _accountService.findById(accountID);
        list.getShared().add(account.getId());
        return this.save(list);
    }

    public ShoppingList stopSharingList(ShoppingList list, String accountID) {
        list.getShared().remove(accountID);
        return this.save(list);
    }

    public ShoppingList toggleArchiveList(String id) throws ShoppingAccessException, ShoppingNotFoundException {
        ShoppingList list = this.findByID(id);
        String currentAccountId = Utils.getCurrentAccountId();
        if (list.getOwnerId().equals(currentAccountId)) {
            list.setArchived(!list.isArchived());
            return this.save(list);
        } else {
            return stopSharingList(list, currentAccountId);
        }
    }

    public void deleteList(String id) throws ShoppingAccessException, ShoppingNotFoundException {
        String currentAccountId = Utils.getCurrentAccountId();
        ShoppingList list = this.findByID(id);
        if (list.getOwnerId().equals(currentAccountId)) {
            this._listRepository.delete(list);
        } else {
            stopSharingList(list, currentAccountId);
        }
    }

    /**
     * Saves passed list
     *
     * @param list list to be saved
     * @return saved/updated list
     */
    public ShoppingList save(ShoppingList list) {
        return this._listRepository.save(list);
    }

    public void sortItems(ShoppingList list) {
        Map<Category, Integer> categoriesOrdered = _propertyService.getCategoriesOrdered();
        Comparator<ListItem> listComparator = Comparator
                .nullsLast(Comparator.comparing((ListItem l) -> categoriesOrdered.get(l.getCategory())));
        list.getItems().sort(listComparator);
    }
}

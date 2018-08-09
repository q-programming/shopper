package com.qprogramming.shopper.app.shoppinglist;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.ShoppingAccessException;
import com.qprogramming.shopper.app.exceptions.ShoppingNotFoundException;
import com.qprogramming.shopper.app.support.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
@Service
public class ShoppingListService {

    private ShoppingListRepository listRepository;
    private AccountService accountService;

    @Autowired
    public ShoppingListService(ShoppingListRepository listRepository, AccountService accountService) {
        this.listRepository = listRepository;
        this.accountService = accountService;
    }

    public Set<ShoppingList> findAllByCurrentUser() throws AccountNotFoundException {
        return findAllByAccountID(Utils.getCurrentAccountId());
    }

    public Set<ShoppingList> findAllByAccountID(String accountId) throws AccountNotFoundException {
        Account account = this.accountService.findById(accountId);
        return this.listRepository.findAllByOwnerIdOrSharedIn(account.getId(), Collections.singleton(account.getId()));
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
        return listRepository.save(list);
    }

    /**
     * returns list by id
     *
     * @param id id of lists to be returned
     * @return Shopping list
     * @throws ShoppingAccessException if currently logged in user don't have access to list
     */
    public ShoppingList findByID(String id) throws ShoppingAccessException, ShoppingNotFoundException {
        Optional<ShoppingList> listOptional = listRepository.findById(Long.valueOf(id));
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
        Account account = accountService.findById(accountID);
        list.getShared().add(account.getId());
        return listRepository.save(list);
    }

    public ShoppingList stopSharingList(ShoppingList list, String accountID) {
        list.getShared().remove(accountID);
        return listRepository.save(list);
    }
}

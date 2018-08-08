package com.qprogramming.shopper.app.shoppinglist;

import com.qprogramming.shopper.app.shoppinglist.exception.ShoppingAccessException;
import com.qprogramming.shopper.app.support.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
@Service
public class ShoppingListService {

    private ShoppingListRepository listRepository;

    @Autowired
    public ShoppingListService(ShoppingListRepository listRepository) {
        this.listRepository = listRepository;
    }

    public List<ShoppingList> findAllByCurrentUser() {
        return this.listRepository.findAllByOwnerId(Utils.getCurrentAccountId());//TODO add shared lists
    }

    public List<ShoppingList> findAllByOwnerID(String ownerID) {
        return this.listRepository.findAllByOwnerId(ownerID).stream().filter(this::canView).collect(Collectors.toList());
    }


    public boolean canView(ShoppingList list) {
        //TODO extend with other list participants later on
        return list.getOwnerId().equals(Utils.getCurrentAccountId());
    }

    public ShoppingList addList(String name) {
        ShoppingList list = new ShoppingList();
        list.setName(name);
        list.setOwnerId(Utils.getCurrentAccountId());
        return listRepository.save(list);
    }

    /**
     * returns list by id
     *
     * @param id id of lists to be returned
     * @return Shopping list
     * @throws ShoppingAccessException if currently logged in user don't have access to list
     */
    public ShoppingList findByID(String id) throws ShoppingAccessException {
        ShoppingList shoppingList = null;
        Optional<ShoppingList> listOptional = listRepository.findById(Long.valueOf(id));
        if (listOptional.isPresent()) {
            shoppingList = listOptional.get();
            if (!canView(shoppingList)) {
                throw new ShoppingAccessException();
            }
        }
        return shoppingList;
    }
}

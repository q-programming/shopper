package com.qprogramming.shopper.app.shoppinglist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Created by Khobar on 05.03.2017.
 */
@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    List<ShoppingList> findAllByOwnerId(String ownerID);

    Set<ShoppingList> findAllByOwnerIdOrSharedIn(String ownerID, Set<String> shared);


}

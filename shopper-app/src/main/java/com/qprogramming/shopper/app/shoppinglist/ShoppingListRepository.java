package com.qprogramming.shopper.app.shoppinglist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Khobar on 05.03.2017.
 */
@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    List<ShoppingList> findAllByOwnerId(String ownerID);

}

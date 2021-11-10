package com.qprogramming.shopper.app.api.items;

import com.qprogramming.shopper.app.exceptions.*;
import com.qprogramming.shopper.app.items.ListItem;
import com.qprogramming.shopper.app.items.ListItemService;
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import com.qprogramming.shopper.app.support.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.qprogramming.shopper.app.exceptions.AccountNotFoundException.ACCOUNT_WITH_ID_WAS_NOT_FOUND;
import static com.qprogramming.shopper.app.exceptions.BadProductNameException.BAD_PRODUCT_NAME;
import static com.qprogramming.shopper.app.exceptions.ItemNotFoundException.ITEM_NOT_FOUND;
import static com.qprogramming.shopper.app.exceptions.ProductNotFoundException.PRODUCT_NOT_FOUND;
import static com.qprogramming.shopper.app.exceptions.ShoppingAccessException.ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID;
import static com.qprogramming.shopper.app.exceptions.ShoppingNotFoundException.SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND;

/**
 * Created by Jakub Romaniszyn on 2018-08-13
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/item")
public class ItemRestController {

    private final ListItemService _listItemService;
    private final ShoppingListService _listService;

    /**
     * Add list item to list with id .
     *
     * @param id shopping lis id
     * @return updated shopping list if operation was successful
     */
    @RequestMapping(value = "/{id}/add", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public ResponseEntity<ShoppingList> addItem(@PathVariable Long id, @RequestBody ListItem item) {
        try {
            ShoppingList list = _listService.findByID(id);
            _listItemService.addItemToList(list, item);
            _listService.sortItems(list);
            return ResponseEntity.ok(_listService.save(list));
        } catch (ProductNotFoundException e) {
            log.error(PRODUCT_NOT_FOUND, item.getProduct().getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (BadProductNameException e) {
            log.error(BAD_PRODUCT_NAME);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ShoppingAccessException e) {
            log.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            log.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Add list item to list with id .
     *
     * @param id shopping lis id
     * @return updated shopping list if operation was successful
     */
    @RequestMapping(value = "/{id}/update", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public ResponseEntity<ShoppingList> updateItem(@PathVariable Long id, @RequestBody ListItem item) {
        try {
            ShoppingList list = _listService.findByID(id);
            ListItem updatedItem = _listItemService.findById(item.getId());
            Product updatedProduct = item.getProduct();
            updatedProduct.setName(updatedProduct.getName().trim());
            if (updatedProduct.equals(updatedItem.getProduct()) ||
                    updatedProduct.getName().equalsIgnoreCase(updatedItem.getProduct().getName())) {//there was product change, delete product and create new
                item = _listItemService.update(item);
            } else {
                _listItemService.replaceProduct(updatedProduct, updatedItem, list, item);
            }
            _listService.sortItems(list);
            return ResponseEntity.ok(_listService.save(list));
        } catch (ShoppingAccessException e) {
            log.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            log.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ItemNotFoundException e) {
            log.error(ITEM_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ProductNotFoundException e) {
            log.error(PRODUCT_NOT_FOUND, item.getProduct().getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (BadProductNameException e) {
            log.error(BAD_PRODUCT_NAME);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (AccountNotFoundException e) {
            log.error(ACCOUNT_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * delete list item from list with id .
     *
     * @param id shopping lis id
     * @return updated shopping list if operation was successful
     */
    @RequestMapping(value = "/{id}/delete", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public ResponseEntity<ShoppingList> deleteItem(@PathVariable Long id, @RequestBody ListItem item) {
        try {
            ShoppingList list = _listService.findByID(id);
            item = _listItemService.findById(item.getId());
            list.getItems().remove(item);
            _listItemService.deleteListItem(item);
            _listService.sortItems(list);
            return ResponseEntity.ok(_listService.save(list));
        } catch (ShoppingAccessException e) {
            log.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            log.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ItemNotFoundException e) {
            log.error(ITEM_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * delete list item from list with id .
     *
     * @param id shopping lis id
     * @return updated shopping list if operation was successful
     */
    @RequestMapping(value = "/{id}/toggle", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public ResponseEntity<ListItem> toggleItem(@PathVariable Long id, @RequestBody ListItem item) {
        try {
            ShoppingList list = _listService.findByID(id);//just verify that list exists and user has access
            item = _listItemService.findById(item.getId());
            return ResponseEntity.ok(_listItemService.toggleItem(item));
        } catch (ShoppingAccessException e) {
            log.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            log.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ItemNotFoundException e) {
            log.error(ITEM_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * get toggle item
     *
     * @param id shopping lis id
     * @return updated shopping list if operation was successful
     * @deprecated Used only by old tizen based companion app
     */
    @RequestMapping(value = "/{id}/toggle/{itemId}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    @Deprecated
    public ResponseEntity<ListItem> toggleItemByID(@PathVariable Long id, @PathVariable Long itemId) {
        try {
            ShoppingList list = _listService.findByID(id);//just verify that list exists and user has access
            ListItem item = _listItemService.findById(itemId);
            return ResponseEntity.ok(_listItemService.toggleItem(item));
        } catch (ShoppingAccessException e) {
            log.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            log.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ItemNotFoundException e) {
            log.error(ITEM_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    /**
     * Get all favorites for currently logged in account but filtered, not to show products that are already on that list,
     * and limited to top 50 most used ones
     *
     * @param id list id
     * @return list of favorite products
     */
    @RequestMapping(value = "/favorites/list/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<Product>> getFavorites(@PathVariable Long id) {
        try {
            ShoppingList list = _listService.findByID(id);
            Set<Product> favorites = _listItemService.getFavoriteProductsForAccount(Utils.getCurrentAccountId());
            return ResponseEntity.ok(_listItemService.filterFavoriteProducts(list, favorites));
        } catch (ShoppingAccessException e) {
            log.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            log.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get all favorite products for currently logged in account
     *
     * @return Set of favorite products
     */
    @RequestMapping(value = "/favorites", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Set<Product>> getAllFavorites() {
        return ResponseEntity.ok(_listItemService.getFavoriteProductsForAccount(Utils.getCurrentAccountId()));
    }

    /**
     * Remove favorite product from account list
     *
     * @param product product to be removed
     * @return list of all favorite products
     */
    @RequestMapping(value = "/favorites/remove", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Set<Product>> removeFromFavorites(@RequestBody Product product) {
        String currentAccountId = Utils.getCurrentAccountId();
        _listItemService.removeFromFavorites(product, currentAccountId);
        return ResponseEntity.ok(_listItemService.getFavoriteProductsForAccount(currentAccountId));
    }

}

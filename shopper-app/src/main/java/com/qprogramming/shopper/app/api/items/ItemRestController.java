package com.qprogramming.shopper.app.api.items;

import com.qprogramming.shopper.app.exceptions.*;
import com.qprogramming.shopper.app.items.ListItem;
import com.qprogramming.shopper.app.items.ListItemService;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import com.qprogramming.shopper.app.support.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import static com.qprogramming.shopper.app.exceptions.BadProductNameException.BAD_PRODUCT_NAME;
import static com.qprogramming.shopper.app.exceptions.ItemNotFoundException.ITEM_NOT_FOUND;
import static com.qprogramming.shopper.app.exceptions.ProductNotFoundException.PRODUCT_NOT_FOUND;
import static com.qprogramming.shopper.app.exceptions.ShoppingAccessException.ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID;
import static com.qprogramming.shopper.app.exceptions.ShoppingNotFoundException.SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND;

/**
 * Created by Jakub Romaniszyn on 2018-08-13
 */
@RestController
@RequestMapping("/api/item")
public class ItemRestController {

    private static final Logger LOG = LoggerFactory.getLogger(ItemRestController.class);
    private ListItemService _listItemService;
    private ShoppingListService _listService;

    @Autowired
    public ItemRestController(ListItemService listItemService, ShoppingListService listService) {
        this._listItemService = listItemService;
        _listService = listService;
    }


    /**
     * Add list item to list with id .
     *
     * @param id shopping lis id
     * @return updated shopping list if operation was successful
     */
    @RequestMapping(value = "/{id}/add", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public ResponseEntity<ShoppingList> addItem(@PathVariable String id, @RequestBody ListItem item) {
        try {
            ShoppingList list = _listService.findByID(id);
            _listItemService.addItemToList(list, item);
            _listService.sortItems(list);
            return ResponseEntity.ok(_listService.save(list));
        } catch (ProductNotFoundException e) {
            LOG.error(PRODUCT_NOT_FOUND, item.getProduct().getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (BadProductNameException e) {
            LOG.error(BAD_PRODUCT_NAME);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ShoppingAccessException e) {
            LOG.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            LOG.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
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
    public ResponseEntity<ListItem> updateItem(@PathVariable String id, @RequestBody ListItem item) {
        try {
            _listService.findByID(id);
            return ResponseEntity.ok(_listItemService.update(item));
        } catch (ShoppingAccessException e) {
            LOG.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            LOG.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ItemNotFoundException e) {
            LOG.error(ITEM_NOT_FOUND, id);
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
    public ResponseEntity<ShoppingList> deleteItem(@PathVariable String id, @RequestBody ListItem item) {
        try {
            ShoppingList list = _listService.findByID(id);
            item = _listItemService.findById(item.getId());
            list.getItems().remove(item);
            _listItemService.deleteListItem(item);
            _listService.sortItems(list);
            return ResponseEntity.ok(_listService.save(list));
        } catch (ShoppingAccessException e) {
            LOG.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            LOG.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ItemNotFoundException e) {
            LOG.error(ITEM_NOT_FOUND, id);
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
    public ResponseEntity<ListItem> toggleItem(@PathVariable String id, @RequestBody ListItem item) {
        try {
            ShoppingList list = _listService.findByID(id);//just verify that list exists and user has access
            item = _listItemService.findById(item.getId());
            return ResponseEntity.ok(_listItemService.toggleItem(item));
        } catch (ShoppingAccessException e) {
            LOG.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            LOG.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ItemNotFoundException e) {
            LOG.error(ITEM_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}

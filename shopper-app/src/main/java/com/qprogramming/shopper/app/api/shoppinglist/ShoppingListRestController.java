package com.qprogramming.shopper.app.api.shoppinglist;

import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.ShoppingAccessException;
import com.qprogramming.shopper.app.exceptions.ShoppingNotFoundException;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import com.qprogramming.shopper.app.support.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

import static com.qprogramming.shopper.app.exceptions.AccountNotFoundException.ACCOUNT_WITH_ID_WAS_NOT_FOUND;
import static com.qprogramming.shopper.app.exceptions.ShoppingAccessException.ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID;
import static com.qprogramming.shopper.app.exceptions.ShoppingNotFoundException.SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
@RestController
@RequestMapping("/api/list")
public class ShoppingListRestController {

    private static final Logger LOG = LoggerFactory.getLogger(ShoppingListRestController.class);
    private ShoppingListService listService;

    @Autowired
    public ShoppingListRestController(ShoppingListService listService) {
        this.listService = listService;
    }

    /**
     * Returns all currently logged in user lists
     *
     * @return List of all lists for which user has access
     */
    @RequestMapping(value = "/mine", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Set<ShoppingList>> getCurrentUserLists(@RequestParam(required = false) boolean archived) {
        try {
            return ResponseEntity.ok(this.listService.findAllByCurrentUser(archived));
        } catch (AccountNotFoundException e) {
            LOG.error(ACCOUNT_WITH_ID_WAS_NOT_FOUND, Utils.getCurrentAccountId());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Returns all currently logged in user lists
     *
     * @return List of all lists for which user has access
     */
    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Set<ShoppingList>> getUsersLists(@PathVariable String id, @RequestParam(required = false) boolean archived) {
        try {
            return ResponseEntity.ok(
                    this.listService.findAllByAccountID(id, archived)
                            .stream()
                            .filter(listService::canView)
                            .collect(Collectors.toSet()));
        } catch (AccountNotFoundException e) {
            LOG.error(ACCOUNT_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Adds new lists with name
     *
     * @param name name of new list
     * @return adds freshly created lists
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ShoppingList> addNewList(@RequestBody String name) {
        return ResponseEntity.ok(this.listService.addList(name));
    }

    /**
     * Returns Shopping list for given id.
     * If shopping list cannot be accessed for currently logged in user , {@link HttpStatus#FORBIDDEN} response will be returned
     *
     * @param id id of shopping list
     * @return Shopping list
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ShoppingList> getList(@PathVariable String id) {
        try {
            ShoppingList list = listService.findByID(id);
            return ResponseEntity.ok(list);
        } catch (ShoppingAccessException e) {
            LOG.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            LOG.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, Utils.getCurrentAccountId());
            return ResponseEntity.notFound().build();
        }

    }

    /**
     * Starts sharing list with id to certain user with accountID
     *
     * @param accountID user which no longer will have list shared
     * @param id        shopping lis id
     * @return modified shopping list
     */
    @RequestMapping(value = "/{id}/share", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ShoppingList> shareList(@RequestBody String accountID, @PathVariable String id) {
        try {
            ShoppingList list = this.listService.findByID(id);
            list = this.listService.shareList(list, accountID);
            return ResponseEntity.ok(list);
        } catch (ShoppingAccessException e) {
            LOG.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (AccountNotFoundException e) {
            LOG.error(ACCOUNT_WITH_ID_WAS_NOT_FOUND, Utils.getCurrentAccountId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ShoppingNotFoundException e) {
            LOG.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Stops sharing list with id to certain user with accountID
     *
     * @param accountID user which no longer will have list shared
     * @param id        shopping lis id
     * @return modified shopping list
     */
    @RequestMapping(value = "/{id}/stop-sharing", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ShoppingList> stopSharingList(@RequestBody String accountID, @PathVariable String id) {
        try {
            ShoppingList list = this.listService.findByID(id);
            list = this.listService.stopSharingList(list, accountID);
            return ResponseEntity.ok(list);
        } catch (ShoppingAccessException e) {
            LOG.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            LOG.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Toggles archived property on list with id .If currently logged in user is not an owner , he/she will just remove himself from shares of that list
     *
     * @param id shopping lis id
     * @return true if operation was success
     */
    @RequestMapping(value = "/{id}/archive", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ShoppingList> archiveList(@PathVariable String id) {
        try {
            return ResponseEntity.ok(this.listService.toggleArchiveList(id));
        } catch (ShoppingAccessException e) {
            LOG.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            LOG.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Delete list with id . If currently logged in user is not an owner , he/she will just remove himself from shares of that list
     *
     * @param id shopping lis id
     * @return true if operation was success
     */
    @RequestMapping(value = "/{id}/delete", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteList(@PathVariable String id) {
        try {
            this.listService.deleteList(id);
            return ResponseEntity.ok().build();
        } catch (ShoppingAccessException e) {
            LOG.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            LOG.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


}

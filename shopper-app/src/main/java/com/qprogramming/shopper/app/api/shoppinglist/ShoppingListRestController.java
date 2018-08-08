package com.qprogramming.shopper.app.api.shoppinglist;

import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import com.qprogramming.shopper.app.shoppinglist.exception.ShoppingAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
@RestController
@RequestMapping("/api/list")
public class ShoppingListRestController {

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
    public ResponseEntity<List<ShoppingList>> getCurrentUserLists() {
        return ResponseEntity.ok(this.listService.findAllByCurrentUser());
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
            if (list != null) {
                return ResponseEntity.ok(list);
            }
            return ResponseEntity.notFound().build();
        } catch (ShoppingAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

    }


}

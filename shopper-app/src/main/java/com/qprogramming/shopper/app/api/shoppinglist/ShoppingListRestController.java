package com.qprogramming.shopper.app.api.shoppinglist;

import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.PresetNotFoundException;
import com.qprogramming.shopper.app.exceptions.ShoppingAccessException;
import com.qprogramming.shopper.app.exceptions.ShoppingNotFoundException;
import com.qprogramming.shopper.app.items.ListItem;
import com.qprogramming.shopper.app.items.ListItemService;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPreset;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPresetService;
import com.qprogramming.shopper.app.support.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.mail.MessagingException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.qprogramming.shopper.app.exceptions.AccountNotFoundException.ACCOUNT_WITH_ID_WAS_NOT_FOUND;
import static com.qprogramming.shopper.app.exceptions.PresetNotFoundException.PRESET_WITH_ID_WAS_NOT_FOUND;
import static com.qprogramming.shopper.app.exceptions.ShoppingAccessException.ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID;
import static com.qprogramming.shopper.app.exceptions.ShoppingNotFoundException.SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
@RestController
@RequestMapping("/api/list")
public class ShoppingListRestController {

    private static final Logger LOG = LoggerFactory.getLogger(ShoppingListRestController.class);
    private ShoppingListService _listService;
    private ListItemService _listItemService;
    private CategoryPresetService _presetService;


    @Autowired
    public ShoppingListRestController(ShoppingListService listService, ListItemService listItemService, CategoryPresetService presetService) {
        this._listService = listService;
        this._listItemService = listItemService;
        this._presetService = presetService;
    }

    /**
     * Returns all currently logged in user lists
     *
     * @return List of all lists for which user has access
     */
    @RequestMapping(value = "/mine", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public ResponseEntity<Set<ShoppingList>> getCurrentUserLists(@RequestParam(required = false) boolean archived, @RequestParam(required = false) boolean items) {
        try {
            Set<ShoppingList> lists = _listService.findAllByCurrentUser(archived);
            return ResponseEntity.ok(getListWithDoneItems(lists, items));
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
    public ResponseEntity<Set<ShoppingList>> getUsersLists(@PathVariable String id, @RequestParam(required = false) boolean archived, @RequestParam(required = false) boolean items) {
        try {
            Set<ShoppingList> lists = _listService.findAllByAccountID(id, archived)
                    .stream()
                    .filter(_listService::canView)
                    .collect(Collectors.toCollection(TreeSet::new));
            return ResponseEntity.ok(getListWithDoneItems(lists, items));

        } catch (AccountNotFoundException e) {
            LOG.error(ACCOUNT_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.notFound().build();
        }
    }


    private Set<ShoppingList> getListWithDoneItems(Set<ShoppingList> lists, boolean items) {
        if (items) {
            lists.forEach(shoppingList -> shoppingList.setDone(shoppingList.getItems().stream().filter(ListItem::isDone).count()));
        }
        return lists;
    }

    /**
     * Adds new lists with name
     *
     * @param list new list
     * @return adds freshly created lists
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ShoppingList> addNewList(@RequestBody ShoppingList list) {
        try {
            return ResponseEntity.ok(_listService.addList(list));
        } catch (PresetNotFoundException e) {
            LOG.error(PRESET_WITH_ID_WAS_NOT_FOUND, list.getPreset().getId());
            return ResponseEntity.notFound().build();
        }
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
    public ResponseEntity<ShoppingList> getList(@PathVariable Long id) {
        try {
            ShoppingList list = _listService.findByID(id);
            _listService.visitList(list);
            _listService.sortItems(list);
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
     * @param email user which no longer will have list shared
     * @param id    shopping lis id
     * @return modified shopping list
     */
    @RequestMapping(value = "/{id}/share", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public ResponseEntity<ShoppingList> shareList(@RequestBody String email, @PathVariable Long id) {
        try {
            ShoppingList list = _listService.findByID(id);
            list = _listService.shareList(list, email);
            return ResponseEntity.ok(list);
        } catch (ShoppingAccessException e) {
            LOG.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            LOG.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (MessagingException e) {
            LOG.error("There was internal error with mailer when trying to send an email: {}", e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
    public ResponseEntity<ShoppingList> stopSharingList(@RequestBody String accountID, @PathVariable Long id) {
        try {
            ShoppingList list = _listService.findByID(id);
            list = _listService.stopSharingList(list, accountID);
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
    public ResponseEntity<ShoppingList> archiveList(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(_listService.toggleArchiveList(id));
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
    public ResponseEntity<?> deleteList(@PathVariable Long id) {
        try {
            _listService.deleteList(id);
            return ResponseEntity.ok().build();
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
     * @param updatedList shopping list to be updated
     * @return true if operation was success
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> editList(@RequestBody ShoppingList updatedList) {
        try {
            if (StringUtils.isBlank(updatedList.getName())) {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            ShoppingList list = _listService.findByID(updatedList.getId());
            return ResponseEntity.ok(_listService.update(list, updatedList));
        } catch (ShoppingAccessException e) {
            LOG.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            LOG.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, updatedList.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (PresetNotFoundException e) {
            LOG.error(PRESET_WITH_ID_WAS_NOT_FOUND, updatedList.getPreset().getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    /**
     * Cleans up shopping list by deleting all items that are marked as done
     *
     * @param id shopping lis id
     * @return shopping list if operation was success
     */
    @RequestMapping(value = "/{id}/cleanup", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ShoppingList> cleanup(@PathVariable Long id) {
        try {
            ShoppingList list = _listService.findByID(id);
            Set<ListItem> toPurge = list.getItems().stream().filter(ListItem::isDone).collect(Collectors.toSet());
            toPurge.forEach(item -> {
                list.getItems().remove(item);
                _listItemService.deleteListItem(item);
            });
            _listService.sortItems(list);
            return ResponseEntity.ok(_listService.save(list));
        } catch (ShoppingAccessException e) {
            LOG.error(ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID, Utils.getCurrentAccountId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ShoppingNotFoundException e) {
            LOG.error(SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @RolesAllowed("ROLE_USER")
    @RequestMapping(value = "/presets", method = RequestMethod.GET)
    public ResponseEntity<List<CategoryPreset>> getCategoryPresets() {
        return ResponseEntity.ok(_presetService.findAllByOwner(Utils.getCurrentAccountId()));
    }


    @RolesAllowed("ROLE_USER")
    @RequestMapping(value = "/presets/update", method = RequestMethod.POST)
    public ResponseEntity<CategoryPreset> updateCategoryPreset(@RequestBody CategoryPreset categoryPreset) {
        if (categoryPreset.getId() == null) {
            categoryPreset.setOwner(Utils.getCurrentAccountId());
            return ResponseEntity.ok(_presetService.save(categoryPreset));
        }
        try {
            CategoryPreset dbPreset = _presetService.findById(categoryPreset.getId());
            String currentAccountId = Utils.getCurrentAccountId();
            if (!dbPreset.getOwner().equals(currentAccountId)) {
                LOG.error("User with id {} tried to remove preset {} for which he is not owner", currentAccountId, dbPreset.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(_presetService.save(categoryPreset));
        } catch (PresetNotFoundException e) {
            LOG.error(PRESET_WITH_ID_WAS_NOT_FOUND, categoryPreset.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @RolesAllowed("ROLE_USER")
    @RequestMapping(value = "/presets/delete", method = RequestMethod.POST)
    public ResponseEntity<CategoryPreset> deleteCategoryPreset(@RequestBody CategoryPreset categoryPreset) {
        try {
            CategoryPreset preset = _presetService.findById(categoryPreset.getId());
            String currentAccountId = Utils.getCurrentAccountId();
            if (!preset.getOwner().equals(currentAccountId)) {
                LOG.error("User with id {} tried to remove preset {} for which he is not owner", currentAccountId, preset.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            _listService.removePresetFromLists(preset);
            _presetService.remove(preset);
            return ResponseEntity.ok().build();
        } catch (PresetNotFoundException e) {
            LOG.error(PRESET_WITH_ID_WAS_NOT_FOUND, categoryPreset.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}

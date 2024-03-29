package com.qprogramming.shopper.app.api.account;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.account.DisplayAccount;
import com.qprogramming.shopper.app.account.devices.Device;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.DeviceNotFoundException;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import com.qprogramming.shopper.app.support.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/account")
public class AccountRestController {
    private final AccountService _accountService;
    private final ShoppingListService _listService;
    private final LogoutHandler _logoutHandler;
    private final CacheManager cacheManager;

    /**
     * Returns currently logged in user as {@link Account}
     *
     * @return currently logged in user
     */
    @RequestMapping("/whoami")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Account user() {
        return (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * Returns all application users
     *
     * @return List of {@link Account}
     */
    @RequestMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<Account> allUsers() {
        return _accountService.findAll();
    }

    /**
     * Returns bytes with Account avatar image
     *
     * @param id Account id for which avatar will be returned
     * @return {@link com.qprogramming.shopper.app.account.avatar.Avatar} account avatar
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/{id}/avatar", method = RequestMethod.GET)
    public ResponseEntity<?> userAvatar(@PathVariable(value = "id") String id) {
        try {
            Account account = _accountService.findById(id);
            return ResponseEntity.ok(_accountService.getAccountAvatar(account));
        } catch (AccountNotFoundException e) {
            log.error("Account for which avatar was searched was not found", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Translates ids into name surname combination
     *
     * @param ids Account ids to be translated into Name Surname
     * @return Set of {@link DisplayAccount}
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public ResponseEntity<Set<DisplayAccount>> getUsers(@RequestBody String[] ids) {
        Set<DisplayAccount> displayAccounts = Arrays.stream(ids).map(s -> {
            try {
                return new DisplayAccount(_accountService.findById(s));
            } catch (AccountNotFoundException e) {
                log.error("Account with id {} was not found", s);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        return ResponseEntity.ok(displayAccounts);
    }

    /**
     * Upload new avatar for currently logged in user
     *
     * @param avatarStream base64 based avatar stream
     * @return {@link HttpStatus#OK} if upload was successful
     */
    @Transactional
    @RequestMapping(value = "/avatar-upload", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> uploadNewAvatar(@RequestBody String avatarStream) {
        Account account = Utils.getCurrentAccount();
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] data = Base64.decodeBase64(avatarStream);
        _accountService.updateAvatar(account, data);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Change settings for currently logged in user
     *
     * @param lang new language
     * @return {@link HttpStatus#OK} if upload was successful
     */
    @Transactional
    @RequestMapping(value = "/settings/language", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> changeLanguage(@RequestBody String lang) {
        Account currentAccount = Utils.getCurrentAccount();
        currentAccount.setLanguage(lang);
        _accountService.update(currentAccount);
        val favoritesCache = cacheManager.getCache("favorites");
        if (favoritesCache != null) {
            favoritesCache.evict(currentAccount.getId());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/settings/favorites", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> changeSortFavorites(@RequestBody boolean sortFavorites) {
        val currentAccount = Utils.getCurrentAccount();
        currentAccount.setSortFavorites(sortFavorites);
        _accountService.update(currentAccount);
        val favoritesCache = cacheManager.getCache("favorites");
        if (favoritesCache != null) {
            favoritesCache.evict(currentAccount.getId());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Sets right mode - checkboxes will be rendered on right side for better mobile experience
     *
     * @param rightmode boolean
     * @return {@link HttpStatus#OK} if upload was successful
     */
    @Transactional
    @RequestMapping(value = "/settings/rightmode", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> changeRightMode(@RequestBody boolean rightmode) {
        Account currentAccount = Utils.getCurrentAccount();
        currentAccount.setRighcheckbox(rightmode);
        _accountService.update(currentAccount);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/settings/devices", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public ResponseEntity<Set<Device>> getDevices() throws AuthenticationException {
        try {
            Account account = _accountService.findById(Utils.getCurrentAccountId());
            return ResponseEntity.ok(account.getDevices());
        } catch (AccountNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/settings/devices/{device}/remove", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public ResponseEntity<?> removeDevice(@PathVariable String device) throws AuthenticationException {
        try {
            _accountService.removeDevice(device);
        } catch (DeviceNotFoundException | AccountNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().build();
    }


    @Transactional
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteAccount(HttpServletRequest requ, HttpServletResponse resp, @RequestBody Account account) {
        if (!account.equals(Utils.getCurrentAccount())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        _listService.transferSharedListOwnership(account);
        _listService.deleteUserLists(account);
        _logoutHandler.logout(requ, resp, SecurityContextHolder.getContext().getAuthentication());
        _accountService.delete(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    @RequestMapping(value = "/friends", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Set<Account>> getFriendList(@RequestParam(required = false) String term) {
        try {
            if (StringUtils.isBlank(term)) {
                return ResponseEntity.ok(_accountService.getAllFriendList());
            }
            return ResponseEntity.ok(_accountService.getAllFriendList()
                    .stream()
                    .filter(account -> account.getEmail().contains(term))
                    .collect(Collectors.toSet()));
        } catch (AccountNotFoundException e) {
            log.error("Account with id {} was not found", Utils.getCurrentAccountId());
            return ResponseEntity.notFound().build();
        }
    }
}
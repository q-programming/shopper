package com.qprogramming.shopper.app.shoppinglist;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.config.mail.Mail;
import com.qprogramming.shopper.app.config.mail.MailService;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.ShoppingAccessException;
import com.qprogramming.shopper.app.exceptions.ShoppingNotFoundException;
import com.qprogramming.shopper.app.items.ListItem;
import com.qprogramming.shopper.app.items.category.Category;
import com.qprogramming.shopper.app.support.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.qprogramming.shopper.app.settings.Settings.APP_EMAIL_FROM;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
@Service
public class ShoppingListService {

    private ShoppingListRepository _listRepository;
    private AccountService _accountService;
    private PropertyService _propertyService;
    private MailService _mailService;


    @Autowired
    public ShoppingListService(ShoppingListRepository listRepository, AccountService accountService, PropertyService propertyService, MailService mailService) {
        this._listRepository = listRepository;
        this._accountService = accountService;
        this._propertyService = propertyService;
        this._mailService = mailService;
    }

    public Set<ShoppingList> findAllByCurrentUser(boolean archived) throws AccountNotFoundException {
        return findAllByAccountID(Utils.getCurrentAccountId(), archived);
    }

    public Set<ShoppingList> findAllByAccountID(String accountId, boolean archived) throws AccountNotFoundException {
        Account account = this._accountService.findById(accountId);
        Set<ShoppingList> list = this._listRepository.findAllByOwnerIdOrSharedIn(account.getId(), Collections.singleton(account.getId()));
        return list.stream().filter(shoppingList -> shoppingList.isArchived() == archived).collect(Collectors.toCollection(TreeSet::new));
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
        list.setLastVisited(new Date());
        return this.save(list);
    }

    /**
     * returns list by id
     *
     * @param id id of lists to be returned
     * @return Shopping list
     * @throws ShoppingAccessException if currently logged in user don't have access to list
     */
    public ShoppingList findByID(String id) throws ShoppingAccessException, ShoppingNotFoundException {
        Optional<ShoppingList> listOptional = _listRepository.findById(Long.valueOf(id));
        if (listOptional.isPresent()) {
            ShoppingList shoppingList = listOptional.get();
            if (!canView(shoppingList)) {
                throw new ShoppingAccessException();
            }
            return shoppingList;
        }
        throw new ShoppingNotFoundException();
    }


    public ShoppingList shareList(ShoppingList list, String email) throws MessagingException {
        Optional<Account> optionalAccount = _accountService.findByEmail(email);
        Mail mail = new Mail();
        mail.setMailTo(email);
        mail.setMailFrom(_propertyService.getProperty(APP_EMAIL_FROM));
        mail.addToModel("owner", Utils.getCurrentAccount().getFullname());
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            mail.addToModel("name", account.getName());
            mail.setLocale(account.getLanguage());
            list.getShared().add(account.getId());
            _mailService.sendShareMessage(mail, list, false);
            //TODO sent email about new list
        } else {
            //just send initiation email
            _mailService.sendShareMessage(mail, list, true);
        }
        return this.save(list);
    }

    public ShoppingList stopSharingList(ShoppingList list, String accountID) {
        list.getShared().remove(accountID);
        return this.save(list);
    }

    public ShoppingList toggleArchiveList(String id) throws ShoppingAccessException, ShoppingNotFoundException {
        ShoppingList list = this.findByID(id);
        String currentAccountId = Utils.getCurrentAccountId();
        if (list.getOwnerId().equals(currentAccountId)) {
            list.setArchived(!list.isArchived());
            return this.save(list);
        } else {
            return stopSharingList(list, currentAccountId);
        }
    }

    public void deleteList(String id) throws ShoppingAccessException, ShoppingNotFoundException {
        String currentAccountId = Utils.getCurrentAccountId();
        ShoppingList list = this.findByID(id);
        if (list.getOwnerId().equals(currentAccountId)) {
            this._listRepository.delete(list);
        } else {
            stopSharingList(list, currentAccountId);
        }
    }

    /**
     * Saves passed list
     *
     * @param list list to be saved
     * @return saved/updated list
     */
    public ShoppingList save(ShoppingList list) {
        return this._listRepository.save(list);
    }

    public void sortItems(ShoppingList list) {
        Map<Category, Integer> categoriesOrdered = _propertyService.getCategoriesOrdered();
        Comparator<ListItem> listComparator = Comparator
                .nullsLast(
                        Comparator.comparing((ListItem l) -> categoriesOrdered.get(l.getCategory()))
                                .thenComparing(listItem -> listItem.getProduct().getName()));
        list.getItems().sort(listComparator);
    }

    public void visitList(ShoppingList list) {
        list.setLastVisited(new Date());
        save(list);
    }
}

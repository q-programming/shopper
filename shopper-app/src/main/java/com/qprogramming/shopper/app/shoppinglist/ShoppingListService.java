package com.qprogramming.shopper.app.shoppinglist;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.config.mail.Mail;
import com.qprogramming.shopper.app.config.mail.MailService;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.PresetNotFoundException;
import com.qprogramming.shopper.app.exceptions.ShoppingAccessException;
import com.qprogramming.shopper.app.exceptions.ShoppingNotFoundException;
import com.qprogramming.shopper.app.items.ListItem;
import com.qprogramming.shopper.app.items.category.Category;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPreset;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPresetRepository;
import com.qprogramming.shopper.app.support.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.qprogramming.shopper.app.settings.Settings.APP_CATEGORY_ORDER;
import static com.qprogramming.shopper.app.settings.Settings.APP_EMAIL_FROM;
import static java.util.stream.Collectors.toMap;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
@Service
public class ShoppingListService {

    private ShoppingListRepository _listRepository;
    private AccountService _accountService;
    private PropertyService _propertyService;
    private MailService _mailService;
    private CategoryPresetRepository presetRepository;


    @Autowired
    public ShoppingListService(ShoppingListRepository listRepository, AccountService accountService, PropertyService propertyService, MailService mailService, CategoryPresetRepository presetRepository) {
        this._listRepository = listRepository;
        this._accountService = accountService;
        this._propertyService = propertyService;
        this._mailService = mailService;
        this.presetRepository = presetRepository;
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

    public ShoppingList addList(ShoppingList list) throws PresetNotFoundException {
        Account currentAccount = Utils.getCurrentAccount();
        list.setOwnerId(currentAccount.getId());
        list.setOwnerName(currentAccount.getName());
        list.setLastVisited(new Date());
        getPreset(list);
        return this.save(list);
    }

    public ShoppingList update(ShoppingList original, ShoppingList updatedList) throws PresetNotFoundException {
        original.setName(updatedList.getName());
        original.setPreset(updatedList.getPreset());
        getPreset(original);
        return this.save(original);
    }

    private void getPreset(ShoppingList list) throws PresetNotFoundException {
        if (list.getPreset() != null) {
            if (list.getPreset().getId() == null) {
                list.setPreset(null);
            } else {
                Optional<CategoryPreset> preset = presetRepository.findById(list.getPreset().getId());
                if (!preset.isPresent()) {
                    throw new PresetNotFoundException();
                }
                list.setPreset(preset.get());
            }
        }
    }

    /**
     * returns list by id
     *
     * @param id id of lists to be returned
     * @return Shopping list
     * @throws ShoppingAccessException if currently logged in user don't have access to list
     */
    public ShoppingList findByID(Long id) throws ShoppingAccessException, ShoppingNotFoundException {
        Optional<ShoppingList> listOptional = _listRepository.findById(id);
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
        } else {
            //add emial to pending and just send initiation email
            list.getPendingshares().add(email);
            _listRepository.save(list);
            _mailService.sendShareMessage(mail, list, true);
        }
        return this.save(list);
    }

    public ShoppingList stopSharingList(ShoppingList list, String accountID) {
        list.getShared().remove(accountID);
        return this.save(list);
    }

    public ShoppingList toggleArchiveList(Long id) throws ShoppingAccessException, ShoppingNotFoundException {
        ShoppingList list = this.findByID(id);
        String currentAccountId = Utils.getCurrentAccountId();
        if (list.getOwnerId().equals(currentAccountId)) {
            list.setArchived(!list.isArchived());
            return this.save(list);
        } else {
            return stopSharingList(list, currentAccountId);
        }
    }

    public void deleteList(Long id) throws ShoppingAccessException, ShoppingNotFoundException {
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
        Map<Category, Integer> categoriesOrdered = getCategoriesOrdered(list);
        Comparator<ListItem> listComparator = Comparator
                .nullsLast(
                        Comparator.comparing((ListItem l) -> categoriesOrdered.get(l.getCategory()))
                                .thenComparing(listItem -> listItem.getProduct().getName().toLowerCase()));
        list.getItems().sort(listComparator);
    }

    public Map<Category, Integer> getCategoriesOrdered(ShoppingList list) {
        String categories;
        if (list.getPreset() == null) {//no list preset load app defaults
            categories = _propertyService.getProperty(APP_CATEGORY_ORDER);
            if (StringUtils.isBlank(categories)) {
                return convertArrayToMap(Category.values());
            }
        } else {
            categories = list.getPreset().getCategoriesOrder();
        }
        return convertArrayToMap(Arrays.stream(categories.split(",")).map(Category::valueOf).toArray(Category[]::new));
    }

    private <T> Map<T, Integer> convertArrayToMap(T[] array) {
        List<T> collection = Arrays.asList(array);
        return IntStream.range(0, collection.size())
                .boxed()
                .collect(toMap(collection::get, i -> i));
    }

    public void visitList(ShoppingList list) {
        list.setLastVisited(new Date());
        save(list);
    }

    public void removePresetFromLists(CategoryPreset preset) {
        List<ShoppingList> shoppingListWithPreset = _listRepository.findAllByPreset(preset);
        shoppingListWithPreset.forEach(shoppingList -> shoppingList.setPreset(null));
        _listRepository.saveAll(shoppingListWithPreset);
    }

    /**
     * Searches for all lists where user potentially is waiting to have acces as shared
     * Afterwards his email is removed from pedningshares
     *
     * @param account Account which will be checked if waiting somewhere to be added
     */
    public void addToListIfPending(Account account) {
        List<ShoppingList> shoppingLists = _listRepository.findAllByPendingshares(account.getEmail());
        shoppingLists.forEach(shoppingList -> {
            shoppingList.getShared().add(account.getId());
            shoppingList.getPendingshares().remove(account.getEmail());
        });
        _listRepository.saveAll(shoppingLists);
    }
}

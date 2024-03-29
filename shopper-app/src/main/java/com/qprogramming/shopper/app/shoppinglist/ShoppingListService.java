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
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.messages.MessagesService;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPreset;
import com.qprogramming.shopper.app.shoppinglist.ordering.CategoryPresetService;
import com.qprogramming.shopper.app.support.Utils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.qprogramming.shopper.app.settings.Settings.APP_CATEGORY_ORDER;
import static com.qprogramming.shopper.app.settings.Settings.APP_EMAIL_FROM;
import static com.qprogramming.shopper.app.support.Utils.not;
import static com.qprogramming.shopper.app.support.Utils.sortByValueAsc;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
@Service
public class ShoppingListService {

    private final ShoppingListRepository _listRepository;
    private final AccountService _accountService;
    private final PropertyService _propertyService;
    private final MessagesService _msgSrv;
    private final MailService _mailService;
    private final CategoryPresetService _presetService;
    @PersistenceContext
    private EntityManager entityManager;


    @Autowired
    public ShoppingListService(ShoppingListRepository listRepository, AccountService accountService, PropertyService propertyService, MessagesService msgSrv, MailService mailService, CategoryPresetService presetService) {
        this._listRepository = listRepository;
        this._accountService = accountService;
        this._propertyService = propertyService;
        this._msgSrv = msgSrv;
        this._mailService = mailService;
        this._presetService = presetService;
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
        CategoryPreset preset = updatedList.getPreset();
        original.setPreset(preset);
        if (preset != null) {
            preset.getOwners().addAll(updatedList.getShared());
        }
        getPreset(original);
        return this.save(original);
    }

    private void getPreset(ShoppingList list) throws PresetNotFoundException {
        if (list.getPreset() != null) {
            if (list.getPreset().getId() == null) {
                list.setPreset(null);
            } else {
                CategoryPreset preset = _presetService.findById(list.getPreset().getId());
                list.setPreset(preset);
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
            shoppingList.setDone(shoppingList.getItems().stream().filter(ListItem::isDone).count());
            return shoppingList;
        }
        throw new ShoppingNotFoundException();
    }

    /**
     * Shares passed list with email.
     * If there already is account with that email, he will be just added to shared list , and list preset will be shared for him as well.
     * If account is not yet there , email will be added to pending shared list .
     * When user registers list with pending shares will be made available for her/him
     *
     * @param list  list to be shared
     * @param email email of recipient
     * @return shared shopping list
     * @throws MessagingException if there was error while sending email
     */
    public ShoppingList shareList(ShoppingList list, String email) throws MessagingException, AccountNotFoundException {
        Optional<Account> optionalAccount = _accountService.findByEmail(email);
        Mail mail = new Mail();
        mail.setMailTo(email);
        mail.setMailFrom(_propertyService.getProperty(APP_EMAIL_FROM));
        mail.addToModel("owner", Utils.getCurrentAccount().getFullname());
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            _accountService.addAccountToFriendList(account);
            mail.addToModel("name", account.getName());
            mail.setLocale(account.getLanguage());
            list.getShared().add(account.getId());
            if (list.getPreset() != null) {
                list.getPreset().getOwners().add(account.getId());
            }
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
                        Comparator
                                .comparing(ListItem::isDone)
                                .thenComparing(listItem -> categoriesOrdered.get(listItem.getCategory()))
                                .thenComparing(listItem -> listItem.getProduct().getName().toLowerCase()));
        list.getItems().sort(listComparator);
    }

    /**
     * Return list of all products grouped into passed list categories, while maintaining order of that products list
     * So 1st product in that list will still be 1st in its top category
     *
     * @param list     shopping lists which category sorter will be used
     * @param products list of products to be grouped
     * @return flat list of all products , still sorted , but now grouped per their top category
     */
    public List<Product> groupProducts(ShoppingList list, List<Product> products) {
        val categoriesOrdered = sortByValueAsc(getCategoriesOrdered(list));
        val grouped = products.stream().collect(groupingBy(Product::getTopCategory));
        return categoriesOrdered
                .keySet()
                .stream()
                .filter(grouped::containsKey)
                .map(grouped::get)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private Map<Category, Integer> getCategoriesOrdered(ShoppingList list) {
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
            if (shoppingList.getPreset() != null) {
                shoppingList.getPreset().getOwners().add(account.getId());
            }
        });
        _listRepository.saveAll(shoppingLists);
    }

    /**
     * Transfer ownership of shared list to first available user, and remove that user from shared list ( as he is now an owner)
     *
     * @param account account for which all lists ownership will be transfered
     */
    public void transferSharedListOwnership(Account account) {
        Set<ShoppingList> accountLists = _listRepository.findAllByOwnerIdOrSharedIn(account.getId(), Collections.singleton(account.getId()));
        accountLists.forEach(shoppingList -> {
            if (shoppingList.getShared().size() > 0) {
                shoppingList.getShared().remove(account.getId());
                processOwnershipTransfer(account, shoppingList);
            }
        });
        _listRepository.saveAll(accountLists);
    }

    private void processOwnershipTransfer(Account account, ShoppingList shoppingList) {
        if (shoppingList.getOwnerId().equals(account.getId())) {
            shoppingList.setOwnerId(shoppingList.getShared().iterator().next());
            shoppingList.getShared().remove(shoppingList.getOwnerId());
        }
    }

    /**
     * Removes all list associated with passed account
     *
     * @param account account for which all lists will be removed
     */
    public void deleteUserLists(Account account) {
        List<ShoppingList> allAccountLists = _listRepository.findAllByOwnerId(account.getId());
        _listRepository.deleteAll(allAccountLists);
    }

    public ShoppingList copyList(Long id) throws ShoppingAccessException, ShoppingNotFoundException {
        ShoppingList list = this.findByID(id);
        ShoppingList copiedList = new ShoppingList();
        Hibernate.initialize(list.getItems());
        getEntityManager().detach(list);
        createListCopy(list, copiedList);
        return _listRepository.save(copiedList);
    }

    private void createListCopy(ShoppingList original, ShoppingList copy) {
        Locale currentLocale = Utils.getCurrentLocale();
        String copyTxt = _msgSrv.getMessage("app.shoppinglist.copy", null, "", currentLocale);
        BeanUtils.copyProperties(original, copy, "id", "items", "shared", "pendingshares");
        copy.setName(copyTxt + " " + original.getName());
        copy.setOwnerId(Utils.getCurrentAccountId());
        original.getItems().forEach(listItem -> {
            getEntityManager().detach(listItem);
            ListItem copiedItem = new ListItem();
            BeanUtils.copyProperties(listItem, copiedItem, "id");
            copy.getItems().add(copiedItem);
        });
    }


    /**
     * Return all favorite products for current account which are not yet on list
     *
     * @return favorite products
     */
    public List<Product> filterFavoriteProducts(ShoppingList list, List<Product> favoriteProductsForAccount) {
        Set<Product> productsAlreadyOnList = list.getItems().stream().map(ListItem::getProduct).collect(Collectors.toSet());
        return favoriteProductsForAccount
                .stream()
                .filter(not(onList(productsAlreadyOnList)))
                .collect(Collectors.toList());
    }

    /**
     * Checks if passed Product is already on passed list
     *
     * @param list list which items will be searched
     * @return true if {@link ShoppingList#getItems()} contains passed product
     */
    public Predicate<Product> onList(Set<Product> list) {
        return p -> list.contains(p) || list.stream().anyMatch(pr -> pr.getName().equalsIgnoreCase(p.getName()));
    }

    /**
     * Visible for testing
     *
     * @return EntityManager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }
}

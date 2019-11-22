package com.qprogramming.shopper.app.items;


import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.BadProductNameException;
import com.qprogramming.shopper.app.exceptions.ItemNotFoundException;
import com.qprogramming.shopper.app.exceptions.ProductNotFoundException;
import com.qprogramming.shopper.app.items.category.Category;
import com.qprogramming.shopper.app.items.favorites.FavoriteProducts;
import com.qprogramming.shopper.app.items.favorites.FavoriteProductsRepository;
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import com.qprogramming.shopper.app.support.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.qprogramming.shopper.app.support.Utils.not;

/**
 * Created by Jakub Romaniszyn on 2018-08-10
 */
@Service
public class ListItemService {
    private static final String UNITS = "(kg|g|l|m|cm|ml|dkg)";
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("(\\d+(\\.|,?)\\d*)" + UNITS + "?$");
    private ListItemRepository _listItemRepository;
    private ProductRepository _productRepository;
    private FavoriteProductsRepository _favoritesRepository;
    private Cache _favoritesCache;
    private static final Logger LOG = LoggerFactory.getLogger(ListItemService.class);

    public ListItemService() {
    }

    @Autowired
    public ListItemService(ListItemRepository listItemRepository, ProductRepository productRepository, FavoriteProductsRepository favoritesRepository, CacheManager cacheManager) {
        this._listItemRepository = listItemRepository;
        this._productRepository = productRepository;
        this._favoritesRepository = favoritesRepository;
        this._favoritesCache = cacheManager.getCache("favorites");
    }

    public ListItem findById(Long id) throws ItemNotFoundException {
        Optional<ListItem> item = _listItemRepository.findById(id);
        return item.orElseThrow(ItemNotFoundException::new);
    }

    /**
     * Creates new list item
     *
     * @param item item DTO to be added
     * @return newly created list item
     * @throws ProductNotFoundException if product was not found in database ( when ID was passed )
     * @throws BadProductNameException  if product was not passed at all , or it's name was empty
     */
    public ListItem createListItem(ListItem item) throws ProductNotFoundException, BadProductNameException {
        if (item.getProduct() != null) {
            item.setName(item.getProduct().getName());
        }
        Product product = getProductOrCreate(item.getProduct());
        updateProductCategoryFromItem(item, product, item.getCategory());
        item.setProduct(product);
        addToFavorites(product);
        return saveItem(item);
    }

    private void updateProductCategoryFromItem(ListItem item, Product product, Category itemCategory) {
        if (itemCategory == null) {
            item.setCategory(product.getTopCategory());
        } else {
            updateCategoryScore(itemCategory, product);
        }
    }

    /**
     * Add List item to list, if item with same product is found on list, it's quantity is increased , or it's marked as undone
     *
     * @param list list to be updated with new item
     * @param item item with product to be added
     * @throws ProductNotFoundException if product was not found
     * @throws BadProductNameException  if product has wrong name or no name at all
     */
    public void addItemToList(ShoppingList list, ListItem item) throws ProductNotFoundException, BadProductNameException {
        ListItem listItem;
        setQuantityFromName(item);
        Optional<ListItem> itemOptional = list.getItems().stream().filter(sameProduct(item.getProduct())).findFirst();
        if (itemOptional.isPresent()) {
            updateExistingItemOnList(item, itemOptional.get());
        } else {
            listItem = createListItem(item);
            list.getItems().add(listItem);
        }
    }

    private void updateExistingItemOnList(ListItem item, ListItem existingItem) {
        if (existingItem.isDone()) {
            existingItem.setDone(false);
        } else {
            existingItem.setQuantity(atLeastOneQuantity(existingItem) + atLeastOneQuantity(item));
            existingItem.setUnit(item.getUnit());
        }
    }

    /**
     * Set quantity from name
     *
     * @param item item which name wil be analysed and quantity extracted from
     */
    public void setQuantityFromName(ListItem item) {
        if (StringUtils.isBlank(item.getProduct().getName())) {
            return;
        }
        String[] parts = item.getProduct().getName().split("\\s+");
        int wordsCount = parts.length;
        if (wordsCount > 1) {
            //check if first and last is number
            String b = parts[0].replace(',', '.');
            String e = parts[wordsCount - 1].replace(',', '.');
            if (isQuantityAndUnit(b) && !isQuantityAndUnit(e)) {
                setQuantityAndUnit(item, b);
                item.getProduct().setName(StringUtils.join(parts, " ", 1, wordsCount));
            } else if (isQuantityAndUnit(e)) {
                setQuantityAndUnit(item, e);
                item.getProduct().setName(StringUtils.join(parts, " ", 0, wordsCount - 1));
            }
        }
    }

    private boolean isQuantityAndUnit(String part) {
        return QUANTITY_PATTERN.matcher(part).matches();
    }

    private void setQuantityAndUnit(ListItem item, String quantityAndUnit) {
        String[] split = quantityAndUnit.split(UNITS);
        String unit = quantityAndUnit.replace(split[0], "");
        item.setQuantity(Float.parseFloat(split[0]));
        item.setUnit(unit);
    }

    private float atLeastOneQuantity(ListItem item) {
        return item.getQuantity() == 0 ? 1 : item.getQuantity();
    }

    public void deleteListItem(ListItem item) {
        _listItemRepository.delete(item);
    }

    private Product getProductOrCreate(Product product) throws ProductNotFoundException, BadProductNameException {
        if (product != null && product.getId() != null) {
            Optional<Product> optionalProduct = _productRepository.findById(product.getId());
            product = optionalProduct.orElseThrow(ProductNotFoundException::new);
        } else {
            if (product == null || StringUtils.isEmpty(product.getName())) {
                throw new BadProductNameException();
            }
            product = getProductByNameOrCreate(product);
        }
        return product;
    }

    private Product getProductByNameOrCreate(Product product) {
        Optional<Product> optionalProduct = _productRepository.findByNameIgnoreCase(product.getName());
        return optionalProduct.orElseGet(() -> saveProduct(product));
    }


    public Product saveProduct(Product product) {
        product.setName(product.getName().trim());
        return this._productRepository.save(product);
    }


    public ListItem saveItem(ListItem item) {
        return this._listItemRepository.save(item);
    }

    /**
     * Update of item from shopping list. If there was product change replace product  will be triggered instead.
     * In case it was just product category change, change it and update score
     * If there in fact was some product change, but for the same product ( but by result eliminating id of product )
     * re-add proper product from database
     *
     * @param item item to be updated
     * @return updated ListItem
     * @throws ItemNotFoundException if item was not found in database
     * @see #replaceProduct(Product, ListItem, ShoppingList, ListItem)
     */
    public ListItem update(ListItem item) throws ItemNotFoundException {
        ListItem listItem = this.findById(item.getId());
        item.setName(item.getProduct().getName());
        if (item.getProduct().getId() == null) {
            item.setProduct(getProductByNameOrCreate(item.getProduct()));
        }
        Category updatedCategory = item.getCategory();
        if (!listItem.getCategory().equals(updatedCategory)) {//there was category change, increase score for that category and this product
            Product product = listItem.getProduct();
            item.setProduct(updateCategoryScore(updatedCategory, product));
        }
        return saveItem(item);
    }

    private Product updateCategoryScore(Category updatedCategory, Product product) {
        Long categoryScore = product.getCategoryScore().getOrDefault(updatedCategory, 0L);
        product.getCategoryScore().put(updatedCategory, ++categoryScore);
        return _productRepository.save(product);
    }

    /**
     * Toggle item as done/not done
     *
     * @param item item to be toggled
     * @return Updated item
     */
    public ListItem toggleItem(ListItem item) {
        item.setDone(!item.isDone());
        return _listItemRepository.save(item);
    }

    /**
     * Predicate to check if passed ListItem#Product is same as the one passed as param
     *
     * @param product product to be checked
     * @return true if product and {@link ListItem#getProduct()} is same
     */
    public Predicate<ListItem> sameProduct(Product product) {
        return i -> i.getProduct().equals(product) || StringUtils.equalsIgnoreCase(product.getName(), i.getProduct().getName());
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
     * Replace product on shopping list with new one
     * First it's evaluated if there is already same product existing on that shopping list
     * If yes , it's being updated with description, unit etc. and it's quantity is increased
     * If no,  replace item with new data, and product
     *
     * @param updatedProduct product to be updated
     * @param updatedItem    item to be updated
     * @param list           list where replace will be happening
     * @param item           database item which is affected
     * @throws ProductNotFoundException If product has id and was not found in database
     * @throws BadProductNameException  If product name is empty
     * @throws AccountNotFoundException If account was not found
     */
    public void replaceProduct(Product updatedProduct, ListItem updatedItem, ShoppingList list, ListItem item) throws ProductNotFoundException, BadProductNameException, AccountNotFoundException {
        updatedItem.setCategory(item.getCategory());
        Optional<ListItem> itemOptional = list.getItems().stream().filter(sameProduct(updatedProduct)).findFirst();
        if (itemOptional.isPresent()) {
            ListItem listItem = itemOptional.get();
            listItem.setQuantity(atLeastOneQuantity(listItem) + atLeastOneQuantity(updatedItem));
            list.getItems().remove(updatedItem);
            deleteListItem(updatedItem);
        } else {
            updatedItem.setName(item.getProduct().getName());
            updatedItem.setDescription(item.getDescription());
            updatedItem.setQuantity(item.getQuantity());
            updatedItem.setUnit(item.getUnit());
            updatedItem.setProduct(getProductOrCreate(updatedProduct));
            updateProductCategoryFromItem(updatedItem, updatedItem.getProduct(), updatedItem.getCategory());
            saveItem(updatedItem);
            addToFavorites(updatedItem.getProduct());
        }
    }

    private void addToFavorites(Product product) {
        String currentAccountId = Utils.getCurrentAccountId();
        FavoriteProducts favoriteProducts = getFavoritesForAccount(currentAccountId);
        Long score = favoriteProducts.getFavorites().getOrDefault(product, 0L);
        if (score == 0) {
            _favoritesCache.evict(currentAccountId);
        }
        favoriteProducts.getFavorites().put(product, ++score);
        _favoritesRepository.save(favoriteProducts);
    }

    private FavoriteProducts getFavoritesForAccount(String currentAccountId) {
        return _favoritesRepository.findById(currentAccountId).orElseGet(() -> {
            LOG.debug("No favorites found for {} , returning default", currentAccountId);
            return new FavoriteProducts(currentAccountId);
        });
    }

    /**
     * Return all favorites products for account
     *
     * @return set of favorite products for current account
     */
    @Cacheable(value = "favorites", key = "#currentAccountId")
    public Set<Product> getFavoriteProductsForAccount(String currentAccountId) {
        return sortByValue(_favoritesRepository.findById(currentAccountId).orElseGet(() -> {
            LOG.debug("No favorites found for {} , returning default", currentAccountId);
            return new FavoriteProducts(currentAccountId);
        }).getFavorites()).keySet();
    }

    /**
     * Return all favorite products for current account which are not yet on list
     *
     * @return favorite products
     */
    public List<Product> filterFavoriteProducts(ShoppingList list, Set<Product> favoriteProductsForAccount) {
        Set<Product> productsAlreadyOnList = list.getItems().stream().map(ListItem::getProduct).collect(Collectors.toSet());
        return favoriteProductsForAccount
                .stream()
                .filter(not(onList(productsAlreadyOnList)))
                .collect(Collectors.toList());
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue
            (Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.<K, V>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Removes passed product from favorites for account with given id
     *
     * @param product          product to be removed from favorites
     * @param currentAccountId id of account
     */
    @CacheEvict(value = "favorites", key = "#currentAccountId")
    public void removeFromFavorites(Product product, String currentAccountId) {
        FavoriteProducts favoriteProducts = getFavoritesForAccount(currentAccountId);
        favoriteProducts.getFavorites().remove(product);
        _favoritesRepository.save(favoriteProducts);
    }
}

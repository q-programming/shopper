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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.qprogramming.shopper.app.support.Utils.not;

/**
 * Created by Jakub Romaniszyn on 2018-08-10
 */
@Service
public class ListItemService {
    private ListItemRepository _listItemRepository;
    private ProductRepository _productRepository;
    private FavoriteProductsRepository _favoritesRepository;
    private static final Logger LOG = LoggerFactory.getLogger(ListItemService.class);

    public ListItemService() {
    }

    @Autowired
    public ListItemService(ListItemRepository listItemRepository, ProductRepository productRepository, FavoriteProductsRepository favoritesRepository) {
        this._listItemRepository = listItemRepository;
        this._productRepository = productRepository;
        this._favoritesRepository = favoritesRepository;
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
    public ListItem createListItem(ListItem item) throws ProductNotFoundException, BadProductNameException, AccountNotFoundException {
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

    public void addItemToList(ShoppingList list, ListItem item) throws ProductNotFoundException, BadProductNameException, AccountNotFoundException {
        ListItem listItem;
        Optional<ListItem> itemOptional = list.getItems().stream().filter(sameProduct(item.getProduct())).findFirst();
        if (itemOptional.isPresent()) {
            listItem = itemOptional.get();
            listItem.setQuantity(atLeastOneQuantity(listItem) + atLeastOneQuantity(item));
            listItem.setDone(false);
        } else {
            listItem = createListItem(item);
            list.getItems().add(listItem);
        }
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
        Optional<Product> optionalProduct = _productRepository.findByNameIgnoreCase(product.getName().trim());
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
     * Update of item from shopping list. If there was product change, whole item will be deleted and readded to list to eliminate any issues with category,product etc.
     * In case it was just product category change, change it and update score
     *
     * @param item item to be updated
     * @return updated ListItem
     * @throws ItemNotFoundException if item was not found in database
     */
    public ListItem update(ListItem item) throws ItemNotFoundException {
        ListItem listItem = this.findById(item.getId());
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

    public ListItem toggleItem(ListItem item) {
        item.setDone(!item.isDone());
        return _listItemRepository.save(item);
    }


    public Predicate<ListItem> sameProduct(Product product) {
        return i -> i.getProduct().equals(product) || StringUtils.equalsIgnoreCase(product.getName(), i.getProduct().getName());
    }

    public Predicate<Product> onList(Set<Product> list) {
        return p -> list.contains(p) || list.stream().anyMatch(pr -> pr.getName().equalsIgnoreCase(p.getName()));
    }

    public void replaceProduct(Product updatedProduct, ListItem updatedItem, ShoppingList list, ListItem item) throws ProductNotFoundException, BadProductNameException, AccountNotFoundException {
        updatedItem.setCategory(item.getCategory());
        Optional<ListItem> itemOptional = list.getItems().stream().filter(sameProduct(updatedProduct)).findFirst();
        if (itemOptional.isPresent()) {
            ListItem listItem = itemOptional.get();
            listItem.setQuantity(atLeastOneQuantity(listItem) + atLeastOneQuantity(updatedItem));
            list.getItems().remove(updatedItem);
            deleteListItem(updatedItem);
        } else {
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
        FavoriteProducts favoriteProducts = getFavoriteProductsForAccount();
        Long score = favoriteProducts.getFavorites().getOrDefault(product, 0L);
        favoriteProducts.getFavorites().put(product, ++score);
        _favoritesRepository.save(favoriteProducts);
    }


    private FavoriteProducts getFavoriteProductsForAccount() {
        String currentAccountId = Utils.getCurrentAccountId();
        Optional<FavoriteProducts> optionalFavorites = _favoritesRepository.findById(currentAccountId);
        if (!optionalFavorites.isPresent()) {
            LOG.debug("No favorites found for {} , returning default", currentAccountId);
            return new FavoriteProducts(currentAccountId);
        }
        return optionalFavorites.get();
    }

    /**
     * Return all favorite products for current account which are not yet on list
     *
     * @return favorite products
     */
    public List<Product> getFavoriteProducts(ShoppingList list) {
        Set<Product> productsAlreadyOnList = list.getItems().stream().map(ListItem::getProduct).collect(Collectors.toSet());
        FavoriteProducts favoriteProductsForAccount = getFavoriteProductsForAccount();
        Map<Product, Long> sortedProducts = sortByValue(favoriteProductsForAccount.getFavorites());
        return sortedProducts.keySet()
                .stream()
                .filter(not(onList(productsAlreadyOnList)))
                .limit(50)
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
}

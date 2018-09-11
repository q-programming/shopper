package com.qprogramming.shopper.app.items;


import com.qprogramming.shopper.app.exceptions.BadProductNameException;
import com.qprogramming.shopper.app.exceptions.ItemNotFoundException;
import com.qprogramming.shopper.app.exceptions.ProductNotFoundException;
import com.qprogramming.shopper.app.items.category.Category;
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by Jakub Romaniszyn on 2018-08-10
 */
@Service
public class ListItemService {
    private ListItemRepository _listItemRepository;
    private ProductRepository _productRepository;

    public ListItemService() {
    }

    @Autowired
    public ListItemService(ListItemRepository listItemRepository, ProductRepository productRepository) {
        this._listItemRepository = listItemRepository;
        this._productRepository = productRepository;
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
        Product product = getProductOrCreate(item.getProduct());
        updateProductCategoryFromItem(item, product, item.getCategory());
        item.setProduct(product);
        return saveItem(item);
    }

    private void updateProductCategoryFromItem(ListItem item, Product product, Category itemCategory) {
        if (itemCategory == null) {
            item.setCategory(product.getTopCategory());
        } else {
            updateCategoryScore(itemCategory, item.getProduct());
        }
    }

    public void addItemToList(ShoppingList list, ListItem item) throws ProductNotFoundException, BadProductNameException {
        ListItem listItem;
        Optional<ListItem> itemOptional = list.getItems().stream().filter(sameProduct(item.getProduct())).findFirst();
        if (itemOptional.isPresent()) {
            listItem = itemOptional.get();
            listItem.setQuantity(atLeastOneQuantity(listItem) + atLeastOneQuantity(item));
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
        Optional<Product> optionalProduct = _productRepository.findByNameIgnoreCase(product.getName());
        return optionalProduct.orElseGet(() -> saveProduct(product));
    }


    public Product saveProduct(Product product) {
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
        Integer categoryScore = product.getCategoryScore().getOrDefault(updatedCategory, 0);
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

    public ListItem replaceProduct(Product updatedProduct, ListItem updatedItem, ShoppingList list) throws ProductNotFoundException, BadProductNameException {
        Optional<ListItem> itemOptional = list.getItems().stream().filter(sameProduct(updatedProduct)).findFirst();
        if (itemOptional.isPresent()) {
            ListItem listItem = itemOptional.get();
            listItem.setQuantity(atLeastOneQuantity(listItem) + atLeastOneQuantity(updatedItem));
            return null;
        } else {
            updatedItem.setProduct(getProductOrCreate(updatedProduct));
            updateProductCategoryFromItem(updatedItem, updatedItem.getProduct(), updatedItem.getCategory());
            return saveItem(updatedItem);
        }
    }
}

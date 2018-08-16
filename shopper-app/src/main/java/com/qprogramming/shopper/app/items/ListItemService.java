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

    public ListItem createListItem(ListItem item) throws ProductNotFoundException, BadProductNameException {
        Product product = getProductOrCreate(item.getProduct());
        Category itemCategory = item.getCategory();
        if (itemCategory == null) {
            item.setCategory(product.getTopCategory());
        } else {
            updateCategoryScore(itemCategory, item.getProduct());
        }
        item.setProduct(product);
        return saveItem(item);
    }

    public void addItemToList(ShoppingList list, ListItem item) throws ProductNotFoundException, BadProductNameException {
        ListItem listItem;
        Optional<ListItem> itemOptional = list.getItems().stream().filter(sameProduct(item.getProduct())).findFirst();
        if (itemOptional.isPresent()) {
            listItem = itemOptional.get();
            listItem.setQuantity(listItem.getQuantity() + item.getQuantity());
        } else {
            listItem = createListItem(item);
            list.getItems().add(listItem);
        }
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
        return i -> i.getProduct().equals(product) || i.getProduct().getName().equalsIgnoreCase(product.getName());
    }
}

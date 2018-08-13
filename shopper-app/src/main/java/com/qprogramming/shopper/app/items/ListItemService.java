package com.qprogramming.shopper.app.items;


import com.qprogramming.shopper.app.exceptions.BadProductNameException;
import com.qprogramming.shopper.app.exceptions.ItemNotFoundException;
import com.qprogramming.shopper.app.exceptions.ProductNotFoundException;
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        if (!item.isPresent()) {
            throw new ItemNotFoundException();
        }
        return item.get();
    }

    public ListItem createListItem(ListItem item) throws ProductNotFoundException, BadProductNameException {
        Product product = getProductOrCreate(item.getProduct());
        item.setProduct(product);
        return saveItem(item);
    }

    public void addItemToList(ShoppingList list, ListItem item) throws ProductNotFoundException, BadProductNameException {
        ListItem listItem;
        Optional<ListItem> itemOptional = list.getItems().stream().filter(streamItem -> streamItem.getProduct().equals(item.getProduct())).findFirst();
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
            if (!optionalProduct.isPresent()) {
                throw new ProductNotFoundException();
            }
            product = optionalProduct.get();
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
        if (optionalProduct.isPresent()) {
            product = optionalProduct.get();
        } else {
            product = this.saveProduct(product);
        }
        return product;
    }


    public Product saveProduct(Product product) {
        return this._productRepository.save(product);
    }


    public ListItem saveItem(ListItem item) {
        return this._listItemRepository.save(item);
    }

    public ListItem update(ListItem item) throws ItemNotFoundException {
        this.findById(item.getId());//just check if item exists
        return saveItem(item);
    }

    public ListItem toggleItem(ListItem item) {
        item.setDone(!item.isDone());
        return _listItemRepository.save(item);
    }
}

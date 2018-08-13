package com.qprogramming.shopper.app.items;


import com.qprogramming.shopper.app.exceptions.BadProductNameException;
import com.qprogramming.shopper.app.exceptions.ProductNotFoundException;
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by Jakub Romaniszyn on 2018-08-10
 */
@Service
public class ListItemService {
    private ListItemRepository listItemRepository;
    private ProductRepository productRepository;

    @Autowired
    public ListItemService(ListItemRepository listItemRepository, ProductRepository productRepository) {
        this.listItemRepository = listItemRepository;
        this.productRepository = productRepository;
    }

    public ListItem createListItem(ListItem item) throws ProductNotFoundException, BadProductNameException {
        Product product = getOrSaveProduct(item.getProduct());
        item.setProduct(product);
        return saveItem(item);
    }

    private Product getOrSaveProduct(Product product) throws ProductNotFoundException, BadProductNameException {
        if (product != null && product.getId() != null) {
            Optional<Product> optionalProduct = this.productRepository.findById(product.getId());
            if (!optionalProduct.isPresent()) {
                throw new ProductNotFoundException();
            }
            product = optionalProduct.get();
        } else {
            if (product == null || StringUtils.isEmpty(product.getName())) {
                throw new BadProductNameException();
            }
            product = this.saveProduct(product);
        }
        return product;
    }


    public Product saveProduct(Product product) {
        return this.productRepository.save(product);
    }


    public ListItem saveItem(ListItem item) {
        return this.listItemRepository.save(item);
    }
}

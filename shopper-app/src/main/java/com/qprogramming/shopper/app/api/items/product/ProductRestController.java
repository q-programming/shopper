package com.qprogramming.shopper.app.api.items.product;

import com.qprogramming.shopper.app.items.category.Category;
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import io.jsonwebtoken.lang.Collections;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static com.qprogramming.shopper.app.support.Utils.getCurrentLanguage;

/**
 * Created by Jakub Romaniszyn on 2018-08-20
 */
@RestController
@RequestMapping("/api/product")
public class ProductRestController {
    private final ProductRepository _productRepository;

    @Autowired
    public ProductRestController(ProductRepository productRepository) {
        this._productRepository = productRepository;
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity getProducts(@RequestParam(required = false) String term) {
        if (StringUtils.isBlank(term)) {
            return ResponseEntity.ok(_productRepository.findAll());
        } else {
            return ResponseEntity.ok(_productRepository.findByNameContainingIgnoreCaseAndLanguage(term, getCurrentLanguage()));
        }
    }


    /**
     * Propose best category for passed product name
     *
     * @param term search term
     * @return top category of first product
     */
    @RequestMapping(value = "/category", method = RequestMethod.GET)
    public ResponseEntity<Category> getCategory(@RequestParam(required = false) String term) {
        if (StringUtils.isBlank(term)) {
            return ResponseEntity.ok(Category.OTHER);
        } else {
            Set<Product> products = _productRepository.findByNameContainingIgnoreCaseAndLanguage(term.trim(), getCurrentLanguage());
            //TODO get the best match in the future. For now just return first one from list. Vetter the term the better result
            return ResponseEntity.ok(Collections.isEmpty(products) ? Category.OTHER : products.iterator().next().getTopCategory());
        }
    }

}

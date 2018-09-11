package com.qprogramming.shopper.app.api.items.product;

import com.qprogramming.shopper.app.items.product.ProductRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Jakub Romaniszyn on 2018-08-20
 */
@RestController
@RequestMapping("/api/product")
public class ProductRestController {
    private ProductRepository _productRepository;

    @Autowired
    public ProductRestController(ProductRepository productRepository) {
        this._productRepository = productRepository;
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity getProducts(@RequestParam(required = false) String term) {
        if (StringUtils.isBlank(term)) {
            return ResponseEntity.ok(_productRepository.findAll());
        } else {
            return ResponseEntity.ok(_productRepository.findByNameContainingIgnoreCase(term));
        }
    }
}

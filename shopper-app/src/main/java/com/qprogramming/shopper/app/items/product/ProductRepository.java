package com.qprogramming.shopper.app.items.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * Created by Jakub Romaniszyn on 09.08.2017.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Set<Product> findByNameContainingIgnoreCaseAndLanguage(String term, String language);

    Optional<Product> findByNameIgnoreCaseAndLanguage(String name, String language);
}

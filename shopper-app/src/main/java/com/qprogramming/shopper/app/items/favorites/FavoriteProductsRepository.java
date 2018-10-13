package com.qprogramming.shopper.app.items.favorites;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Jakub Romaniszyn on 2018-10-05
 */
public interface FavoriteProductsRepository extends JpaRepository<FavoriteProducts, String> {

}

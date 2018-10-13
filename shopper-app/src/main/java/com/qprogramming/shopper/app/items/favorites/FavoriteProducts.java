package com.qprogramming.shopper.app.items.favorites;

import com.qprogramming.shopper.app.items.product.Product;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jakub Romaniszyn on 2018-10-05
 */
@Entity
public class FavoriteProducts {

    @Id
    private String id;

    @ElementCollection
    @CollectionTable(name = "favorites_count", joinColumns = @JoinColumn(name = "account_id"))
    private Map<Product, Long> favorites = new HashMap<>();

    public FavoriteProducts() {
    }

    public FavoriteProducts(String id) {
        this.id = id;
    }

    public Map<Product, Long> getFavorites() {
        if (favorites.isEmpty()) {
            favorites = new HashMap<>();
        }
        return favorites;
    }

    public void setFavorites(Map<Product, Long> favorites) {
        this.favorites = favorites;
    }

    public String getId() {
        return id;
    }
}

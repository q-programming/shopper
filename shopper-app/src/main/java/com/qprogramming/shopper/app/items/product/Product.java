package com.qprogramming.shopper.app.items.product;

import com.qprogramming.shopper.app.items.category.Category;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Jakub Romaniszyn on 2018-08-10
 */
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq_gen")
    @SequenceGenerator(name = "product_seq_gen", sequenceName = "product_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    //TODO top category
    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "score")
    @CollectionTable(name = "category_score", joinColumns = @JoinColumn(name = "product_id"))
    private Map<Category, Integer> categoryScore = new HashMap<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Category, Integer> getCategoryScore() {
        return categoryScore;
    }

    public void setCategoryScore(Map<Category, Integer> categoryScore) {
        this.categoryScore = categoryScore;
    }

    @JsonProperty("top-category")
    public Category getTopCategory() {
        if (!categoryScore.isEmpty()) {
            return Collections.max(categoryScore.entrySet(), Map.Entry.comparingByValue()).getKey();
        }
        return Category.OTHER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id) &&
                name.equalsIgnoreCase(product.name);

    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
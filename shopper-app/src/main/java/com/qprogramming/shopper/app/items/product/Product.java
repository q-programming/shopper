package com.qprogramming.shopper.app.items.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qprogramming.shopper.app.items.category.Category;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Jakub Romaniszyn on 2018-08-10
 */
@Entity
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq_gen")
    @SequenceGenerator(name = "product_seq_gen", sequenceName = "product_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @Column
    private String language;


    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "score")
    @CollectionTable(name = "category_score", joinColumns = @JoinColumn(name = "product_id"))
    private Map<Category, Long> categoryScore = new HashMap<>();

    public String getName() {
        return StringUtils.isNotBlank(name) ? name.trim() : name;
    }

    public Map<Category, Long> getCategoryScore() {
        if (categoryScore.isEmpty()) {
            categoryScore = new HashMap<>();
        }
        return categoryScore;
    }

    public void setCategoryScore(Map<Category, Long> categoryScore) {
        this.categoryScore = categoryScore;
    }

    @JsonProperty("topCategory")
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
                Objects.equals(name, product.name) &&
                Objects.equals(categoryScore, product.categoryScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, categoryScore);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

package com.qprogramming.shopper.app.shoppinglist.ordering;

import javax.persistence.*;

/**
 * Created by Jakub Romaniszyn on 2018-09-26
 */
@Entity
public class CategoryPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "preset_seq_gen")
    @SequenceGenerator(name = "preset_seq_gen", sequenceName = "preset_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @Column
    private String owner;

    @Column(length = 1000)
    private String categoriesOrder;


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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCategoriesOrder() {
        return categoriesOrder;
    }

    public void setCategoriesOrder(String categoriesOrder) {
        this.categoriesOrder = categoriesOrder;
    }
}

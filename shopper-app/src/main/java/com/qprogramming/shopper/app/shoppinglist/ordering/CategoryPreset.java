package com.qprogramming.shopper.app.shoppinglist.ordering;

import io.jsonwebtoken.lang.Collections;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

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
    private String ownername;

    @Column
    private String ownerId;

    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "owners")
    private Set<String> owners;


    @Column(length = 1000)
    private String categoriesOrder;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnername() {
        return ownername;
    }

    public void setOwnername(String ownername) {
        this.ownername = ownername;
    }

    public String getCategoriesOrder() {
        return categoriesOrder;
    }

    public void setCategoriesOrder(String categoriesOrder) {
        this.categoriesOrder = categoriesOrder;
    }

    public Set<String> getOwners() {
        if (Collections.isEmpty(owners)) {
            this.owners = new HashSet<>();
        }
        return owners;
    }

    public void setOwners(Set<String> owners) {
        this.owners = owners;
    }
}

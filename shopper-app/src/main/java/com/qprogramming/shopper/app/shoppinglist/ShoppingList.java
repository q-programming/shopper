package com.qprogramming.shopper.app.shoppinglist;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

import static com.qprogramming.shopper.app.support.Utils.SHOPPING_LIST_COMPARATOR;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
@Entity
public class ShoppingList implements Serializable, Comparable<ShoppingList> {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String ownerId;

    @Column
    private boolean archived;

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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingList that = (ShoppingList) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(ownerId, that.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, ownerId);
    }

    @Override
    public int compareTo(ShoppingList o) {
        return SHOPPING_LIST_COMPARATOR.compare(this, o);
    }

    @Override
    public String toString() {
        return "ShoppingList{" +
                "name='" + name + '\'' +
                ", ownerId='" + ownerId + '\'' +
                '}';
    }
}

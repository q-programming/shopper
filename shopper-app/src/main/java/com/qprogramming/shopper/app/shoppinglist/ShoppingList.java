package com.qprogramming.shopper.app.shoppinglist;

import com.qprogramming.shopper.app.items.ListItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static com.qprogramming.shopper.app.support.Utils.SHOPPING_LIST_COMPARATOR;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 */
@Entity
public class ShoppingList implements Serializable, Comparable<ShoppingList> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "list_seq_gen")
    @SequenceGenerator(name = "list_seq_gen", sequenceName = "list_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @Column
    private String ownerId;

    @Column
    private String ownerName;

    @Column
    private boolean archived;

    @ElementCollection
    private Set<String> shared = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ListItem> items = new ArrayList<>();

    @Column
    private Date lastVisited;

    private Long done;

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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Set<String> getShared() {
        return shared;
    }

    public void setShared(Set<String> shared) {
        this.shared = shared;
    }

    public List<ListItem> getItems() {
        return items;
    }

    public void setItems(List<ListItem> items) {
        this.items = items;
    }

    public Long getDone() {
        return done;
    }

    public void setDone(Long done) {
        this.done = done;
    }

    public Date getLastVisited() {
        return lastVisited;
    }

    public void setLastVisited(Date lastVisited) {
        this.lastVisited = lastVisited;
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

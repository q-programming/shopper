package com.qprogramming.shopper.app.items;

import com.qprogramming.shopper.app.items.product.Product;

import javax.persistence.*;
import java.util.Objects;

/**
 * Created by Jakub Romaniszyn on 2018-08-10
 */
@Entity
public class ListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_seq_gen")
    @SequenceGenerator(name = "item_seq_gen", sequenceName = "item_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    private Product product;

    @Column(length = 1000)
    private String description;

    @Column
    private int quantity;

    @Column
    private String unit;

    private boolean done;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public String toString() {
        return "ListItem{" +
                "id=" + id +
                ", product=" + product +
                ", quantity=" + quantity +
                ", done=" + done +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListItem listItem = (ListItem) o;
        return quantity == listItem.quantity &&
                done == listItem.done &&
                Objects.equals(id, listItem.id) &&
                Objects.equals(product, listItem.product) &&
                Objects.equals(description, listItem.description) &&
                Objects.equals(unit, listItem.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, product, description, quantity, unit, done);
    }
}

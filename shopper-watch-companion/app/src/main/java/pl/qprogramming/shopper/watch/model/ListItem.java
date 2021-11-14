package pl.qprogramming.shopper.watch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListItem {
    private Long id;
    private Product product;
    private String name;
    private String description;
    private float quantity;
    private String unit;
    private Category category;
    private boolean done;
}
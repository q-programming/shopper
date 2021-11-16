package pl.qprogramming.shopper.watch.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingList {
    private Long id;
    private String name;
    private String ownerName;
    private boolean archived;
    private CategoryPreset preset;
    private Long done;
    private List<ListItem> items;
}

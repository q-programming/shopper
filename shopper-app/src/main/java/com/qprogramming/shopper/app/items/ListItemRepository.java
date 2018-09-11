package com.qprogramming.shopper.app.items;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Jakub Romaniszyn on 09.08.2017.
 */
@Repository
public interface ListItemRepository extends JpaRepository<ListItem, Long> {
}

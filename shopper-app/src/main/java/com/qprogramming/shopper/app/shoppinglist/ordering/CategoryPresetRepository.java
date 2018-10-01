package com.qprogramming.shopper.app.shoppinglist.ordering;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Khobar on 05.03.2017.
 */
@Repository
public interface CategoryPresetRepository extends JpaRepository<CategoryPreset, Long> {

    List<CategoryPreset> findAllByOwner(String owner);




}

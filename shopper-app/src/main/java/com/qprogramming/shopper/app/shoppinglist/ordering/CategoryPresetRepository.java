package com.qprogramming.shopper.app.shoppinglist.ordering;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Created by Khobar on 05.03.2017.
 */
@Repository
public interface CategoryPresetRepository extends JpaRepository<CategoryPreset, Long> {

    Set<CategoryPreset> findAllByOwnername(String owner);

    Set<CategoryPreset> findAllByOwners(String owner);

    Set<CategoryPreset> findAllByOwnerIdOrOwnersIn(String owener, Set<String> owners);


}

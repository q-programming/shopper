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

    List<CategoryPreset> findAllByOwnername(String owner);

    List<CategoryPreset> findAllByOwners(String owner);

    List<CategoryPreset> findAllByOwnerIdOrOwnersIn(String owener, Set<String> owners);


}

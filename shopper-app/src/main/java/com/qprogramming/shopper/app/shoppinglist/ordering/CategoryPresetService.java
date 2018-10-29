package com.qprogramming.shopper.app.shoppinglist.ordering;

import com.qprogramming.shopper.app.exceptions.PresetNotFoundException;
import com.qprogramming.shopper.app.support.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Jakub Romaniszyn on 2018-09-28
 */
@Service
public class CategoryPresetService {

    private CategoryPresetRepository _presetRepository;

    @Autowired
    public CategoryPresetService(CategoryPresetRepository presetRepository) {
        this._presetRepository = presetRepository;
    }

    /**
     * FInd all Category presets where ownerID is equal to accountId
     *
     * @param accountId id for which presets is owner
     * @return list of category presets
     */
    public Set<CategoryPreset> findAllByOwner(String accountId) {
        return _presetRepository.findAllByOwnerIdOrOwnersIn(accountId, Collections.singleton(accountId));
    }

    /**
     * Create or update Category Preset
     *
     * @param categoryPreset preset to be saved
     * @return updated entity of preset
     */
    public CategoryPreset save(CategoryPreset categoryPreset) {
        return _presetRepository.save(categoryPreset);
    }

    /**
     * Finds category preset by id. If it was not found , exception is thrown
     *
     * @param id Category preset id
     * @return searched preset
     * @throws PresetNotFoundException if preset with this id was not found in database
     */
    public CategoryPreset findById(Long id) throws PresetNotFoundException {
        Optional<CategoryPreset> presetOptional = _presetRepository.findById(id);
        if (!presetOptional.isPresent()) {
            throw new PresetNotFoundException();
        }
        return presetOptional.get();
    }

    /**
     * Remove category preset
     *
     * @param preset Category preset to be removed
     */
    public void remove(CategoryPreset preset) {

        _presetRepository.delete(preset);
    }

    public CategoryPreset create(CategoryPreset categoryPreset) {
        categoryPreset.setOwnerId(Utils.getCurrentAccountId());
        categoryPreset.getOwners().add(Utils.getCurrentAccountId());
        categoryPreset.setOwnername(Utils.getCurrentAccount().getName());
        return save(categoryPreset);
    }

    public boolean canAccess(CategoryPreset preset, String accountID) {
        return preset.getOwnerId().equals(accountID) || preset.getOwners().contains(accountID);
    }
}

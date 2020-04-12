package com.azrul.ebanking.banking.service;

import com.azrul.ebanking.banking.service.dto.ProductAccountDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link com.azrul.ebanking.banking.domain.ProductAccount}.
 */
public interface ProductAccountService {

    /**
     * Save a productAccount.
     *
     * @param productAccountDTO the entity to save.
     * @return the persisted entity.
     */
    ProductAccountDTO save(ProductAccountDTO productAccountDTO);

    /**
     * Get all the productAccounts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ProductAccountDTO> findAll(Pageable pageable);

    /**
     * Get the "id" productAccount.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ProductAccountDTO> findOne(Long id);

    /**
     * Delete the "id" productAccount.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}

package com.azrul.ebanking.banking.service.impl;

import com.azrul.ebanking.banking.service.ProductAccountService;
import com.azrul.ebanking.banking.domain.ProductAccount;
import com.azrul.ebanking.banking.repository.ProductAccountRepository;
import com.azrul.ebanking.banking.service.dto.ProductAccountDTO;
import com.azrul.ebanking.banking.service.mapper.ProductAccountMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link ProductAccount}.
 */
@Service
@Transactional
public class ProductAccountServiceImpl implements ProductAccountService {

    private final Logger log = LoggerFactory.getLogger(ProductAccountServiceImpl.class);

    private final ProductAccountRepository productAccountRepository;

    private final ProductAccountMapper productAccountMapper;

    public ProductAccountServiceImpl(ProductAccountRepository productAccountRepository, ProductAccountMapper productAccountMapper) {
        this.productAccountRepository = productAccountRepository;
        this.productAccountMapper = productAccountMapper;
    }

    /**
     * Save a productAccount.
     *
     * @param productAccountDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public ProductAccountDTO save(ProductAccountDTO productAccountDTO) {
        log.debug("Request to save ProductAccount : {}", productAccountDTO);
        ProductAccount productAccount = productAccountMapper.toEntity(productAccountDTO);
        productAccount = productAccountRepository.save(productAccount);
        return productAccountMapper.toDto(productAccount);
    }

    /**
     * Get all the productAccounts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductAccountDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ProductAccounts");
        return productAccountRepository.findAll(pageable)
            .map(productAccountMapper::toDto);
    }

    /**
     * Get one productAccount by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ProductAccountDTO> findOne(Long id) {
        log.debug("Request to get ProductAccount : {}", id);
        return productAccountRepository.findById(id)
            .map(productAccountMapper::toDto);
    }

    /**
     * Delete the productAccount by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ProductAccount : {}", id);
        productAccountRepository.deleteById(id);
    }
}

package com.azrul.ebanking.banking.web.rest;

import com.azrul.ebanking.banking.service.ProductAccountService;
import com.azrul.ebanking.banking.web.rest.errors.BadRequestAlertException;
import com.azrul.ebanking.banking.service.dto.ProductAccountDTO;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.azrul.ebanking.banking.domain.ProductAccount}.
 */
@RestController
@RequestMapping("/api")
public class ProductAccountResource {

    private final Logger log = LoggerFactory.getLogger(ProductAccountResource.class);

    private static final String ENTITY_NAME = "bankingProductAccount";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ProductAccountService productAccountService;

    public ProductAccountResource(ProductAccountService productAccountService) {
        this.productAccountService = productAccountService;
    }

    /**
     * {@code POST  /product-accounts} : Create a new productAccount.
     *
     * @param productAccountDTO the productAccountDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new productAccountDTO, or with status {@code 400 (Bad Request)} if the productAccount has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/product-accounts")
    public ResponseEntity<ProductAccountDTO> createProductAccount(@RequestBody ProductAccountDTO productAccountDTO) throws URISyntaxException {
        log.debug("REST request to save ProductAccount : {}", productAccountDTO);
        if (productAccountDTO.getId() != null) {
            throw new BadRequestAlertException("A new productAccount cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ProductAccountDTO result = productAccountService.save(productAccountDTO);
        return ResponseEntity.created(new URI("/api/product-accounts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /product-accounts} : Updates an existing productAccount.
     *
     * @param productAccountDTO the productAccountDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated productAccountDTO,
     * or with status {@code 400 (Bad Request)} if the productAccountDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the productAccountDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/product-accounts")
    public ResponseEntity<ProductAccountDTO> updateProductAccount(@RequestBody ProductAccountDTO productAccountDTO) throws URISyntaxException {
        log.debug("REST request to update ProductAccount : {}", productAccountDTO);
        if (productAccountDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        ProductAccountDTO result = productAccountService.save(productAccountDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, productAccountDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /product-accounts} : get all the productAccounts.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of productAccounts in body.
     */
    @GetMapping("/product-accounts")
    public ResponseEntity<List<ProductAccountDTO>> getAllProductAccounts(Pageable pageable) {
        log.debug("REST request to get a page of ProductAccounts");
        Page<ProductAccountDTO> page = productAccountService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /product-accounts/:id} : get the "id" productAccount.
     *
     * @param id the id of the productAccountDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the productAccountDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/product-accounts/{id}")
    public ResponseEntity<ProductAccountDTO> getProductAccount(@PathVariable Long id) {
        log.debug("REST request to get ProductAccount : {}", id);
        Optional<ProductAccountDTO> productAccountDTO = productAccountService.findOne(id);
        return ResponseUtil.wrapOrNotFound(productAccountDTO);
    }

    /**
     * {@code DELETE  /product-accounts/:id} : delete the "id" productAccount.
     *
     * @param id the id of the productAccountDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/product-accounts/{id}")
    public ResponseEntity<Void> deleteProductAccount(@PathVariable Long id) {
        log.debug("REST request to delete ProductAccount : {}", id);
        productAccountService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}

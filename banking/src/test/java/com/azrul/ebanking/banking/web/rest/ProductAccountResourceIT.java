package com.azrul.ebanking.banking.web.rest;

import com.azrul.ebanking.banking.BankingApp;
import com.azrul.ebanking.banking.config.TestSecurityConfiguration;
import com.azrul.ebanking.banking.domain.ProductAccount;
import com.azrul.ebanking.banking.repository.ProductAccountRepository;
import com.azrul.ebanking.banking.service.ProductAccountService;
import com.azrul.ebanking.banking.service.dto.ProductAccountDTO;
import com.azrul.ebanking.banking.service.mapper.ProductAccountMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.List;

import static com.azrul.ebanking.banking.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link ProductAccountResource} REST controller.
 */
@SpringBootTest(classes = { BankingApp.class, TestSecurityConfiguration.class })

@AutoConfigureMockMvc
@WithMockUser
public class ProductAccountResourceIT {

    private static final String DEFAULT_ACCOUNT_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_ACCOUNT_NUMBER = "BBBBBBBBBB";

    private static final String DEFAULT_PRODUCT_ID = "AAAAAAAAAA";
    private static final String UPDATED_PRODUCT_ID = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_OPENING_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_OPENING_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final Integer DEFAULT_STATUS = 1;
    private static final Integer UPDATED_STATUS = 2;

    private static final BigDecimal DEFAULT_BALANCE = new BigDecimal(1);
    private static final BigDecimal UPDATED_BALANCE = new BigDecimal(2);

    @Autowired
    private ProductAccountRepository productAccountRepository;

    @Autowired
    private ProductAccountMapper productAccountMapper;

    @Autowired
    private ProductAccountService productAccountService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProductAccountMockMvc;

    private ProductAccount productAccount;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductAccount createEntity(EntityManager em) {
        ProductAccount productAccount = new ProductAccount()
            .accountNumber(DEFAULT_ACCOUNT_NUMBER)
            .productId(DEFAULT_PRODUCT_ID)
            .openingDate(DEFAULT_OPENING_DATE)
            .status(DEFAULT_STATUS)
            .balance(DEFAULT_BALANCE);
        return productAccount;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductAccount createUpdatedEntity(EntityManager em) {
        ProductAccount productAccount = new ProductAccount()
            .accountNumber(UPDATED_ACCOUNT_NUMBER)
            .productId(UPDATED_PRODUCT_ID)
            .openingDate(UPDATED_OPENING_DATE)
            .status(UPDATED_STATUS)
            .balance(UPDATED_BALANCE);
        return productAccount;
    }

    @BeforeEach
    public void initTest() {
        productAccount = createEntity(em);
    }

    @Test
    @Transactional
    public void createProductAccount() throws Exception {
        int databaseSizeBeforeCreate = productAccountRepository.findAll().size();

        // Create the ProductAccount
        ProductAccountDTO productAccountDTO = productAccountMapper.toDto(productAccount);
        restProductAccountMockMvc.perform(post("/api/product-accounts").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(productAccountDTO)))
            .andExpect(status().isCreated());

        // Validate the ProductAccount in the database
        List<ProductAccount> productAccountList = productAccountRepository.findAll();
        assertThat(productAccountList).hasSize(databaseSizeBeforeCreate + 1);
        ProductAccount testProductAccount = productAccountList.get(productAccountList.size() - 1);
        assertThat(testProductAccount.getAccountNumber()).isEqualTo(DEFAULT_ACCOUNT_NUMBER);
        assertThat(testProductAccount.getProductId()).isEqualTo(DEFAULT_PRODUCT_ID);
        assertThat(testProductAccount.getOpeningDate()).isEqualTo(DEFAULT_OPENING_DATE);
        assertThat(testProductAccount.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testProductAccount.getBalance()).isEqualTo(DEFAULT_BALANCE);
    }

    @Test
    @Transactional
    public void createProductAccountWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = productAccountRepository.findAll().size();

        // Create the ProductAccount with an existing ID
        productAccount.setId(1L);
        ProductAccountDTO productAccountDTO = productAccountMapper.toDto(productAccount);

        // An entity with an existing ID cannot be created, so this API call must fail
        restProductAccountMockMvc.perform(post("/api/product-accounts").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(productAccountDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ProductAccount in the database
        List<ProductAccount> productAccountList = productAccountRepository.findAll();
        assertThat(productAccountList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllProductAccounts() throws Exception {
        // Initialize the database
        productAccountRepository.saveAndFlush(productAccount);

        // Get all the productAccountList
        restProductAccountMockMvc.perform(get("/api/product-accounts?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(productAccount.getId().intValue())))
            .andExpect(jsonPath("$.[*].accountNumber").value(hasItem(DEFAULT_ACCOUNT_NUMBER)))
            .andExpect(jsonPath("$.[*].productId").value(hasItem(DEFAULT_PRODUCT_ID)))
            .andExpect(jsonPath("$.[*].openingDate").value(hasItem(sameInstant(DEFAULT_OPENING_DATE))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS)))
            .andExpect(jsonPath("$.[*].balance").value(hasItem(DEFAULT_BALANCE.intValue())));
    }
    
    @Test
    @Transactional
    public void getProductAccount() throws Exception {
        // Initialize the database
        productAccountRepository.saveAndFlush(productAccount);

        // Get the productAccount
        restProductAccountMockMvc.perform(get("/api/product-accounts/{id}", productAccount.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(productAccount.getId().intValue()))
            .andExpect(jsonPath("$.accountNumber").value(DEFAULT_ACCOUNT_NUMBER))
            .andExpect(jsonPath("$.productId").value(DEFAULT_PRODUCT_ID))
            .andExpect(jsonPath("$.openingDate").value(sameInstant(DEFAULT_OPENING_DATE)))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS))
            .andExpect(jsonPath("$.balance").value(DEFAULT_BALANCE.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingProductAccount() throws Exception {
        // Get the productAccount
        restProductAccountMockMvc.perform(get("/api/product-accounts/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateProductAccount() throws Exception {
        // Initialize the database
        productAccountRepository.saveAndFlush(productAccount);

        int databaseSizeBeforeUpdate = productAccountRepository.findAll().size();

        // Update the productAccount
        ProductAccount updatedProductAccount = productAccountRepository.findById(productAccount.getId()).get();
        // Disconnect from session so that the updates on updatedProductAccount are not directly saved in db
        em.detach(updatedProductAccount);
        updatedProductAccount
            .accountNumber(UPDATED_ACCOUNT_NUMBER)
            .productId(UPDATED_PRODUCT_ID)
            .openingDate(UPDATED_OPENING_DATE)
            .status(UPDATED_STATUS)
            .balance(UPDATED_BALANCE);
        ProductAccountDTO productAccountDTO = productAccountMapper.toDto(updatedProductAccount);

        restProductAccountMockMvc.perform(put("/api/product-accounts").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(productAccountDTO)))
            .andExpect(status().isOk());

        // Validate the ProductAccount in the database
        List<ProductAccount> productAccountList = productAccountRepository.findAll();
        assertThat(productAccountList).hasSize(databaseSizeBeforeUpdate);
        ProductAccount testProductAccount = productAccountList.get(productAccountList.size() - 1);
        assertThat(testProductAccount.getAccountNumber()).isEqualTo(UPDATED_ACCOUNT_NUMBER);
        assertThat(testProductAccount.getProductId()).isEqualTo(UPDATED_PRODUCT_ID);
        assertThat(testProductAccount.getOpeningDate()).isEqualTo(UPDATED_OPENING_DATE);
        assertThat(testProductAccount.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testProductAccount.getBalance()).isEqualTo(UPDATED_BALANCE);
    }

    @Test
    @Transactional
    public void updateNonExistingProductAccount() throws Exception {
        int databaseSizeBeforeUpdate = productAccountRepository.findAll().size();

        // Create the ProductAccount
        ProductAccountDTO productAccountDTO = productAccountMapper.toDto(productAccount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductAccountMockMvc.perform(put("/api/product-accounts").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(productAccountDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ProductAccount in the database
        List<ProductAccount> productAccountList = productAccountRepository.findAll();
        assertThat(productAccountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteProductAccount() throws Exception {
        // Initialize the database
        productAccountRepository.saveAndFlush(productAccount);

        int databaseSizeBeforeDelete = productAccountRepository.findAll().size();

        // Delete the productAccount
        restProductAccountMockMvc.perform(delete("/api/product-accounts/{id}", productAccount.getId()).with(csrf())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ProductAccount> productAccountList = productAccountRepository.findAll();
        assertThat(productAccountList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

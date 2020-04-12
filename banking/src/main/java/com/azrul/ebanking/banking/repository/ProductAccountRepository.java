package com.azrul.ebanking.banking.repository;

import com.azrul.ebanking.banking.domain.ProductAccount;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the ProductAccount entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProductAccountRepository extends JpaRepository<ProductAccount, Long> {
}

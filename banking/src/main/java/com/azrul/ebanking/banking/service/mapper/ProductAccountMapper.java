package com.azrul.ebanking.banking.service.mapper;


import com.azrul.ebanking.banking.domain.*;
import com.azrul.ebanking.banking.service.dto.ProductAccountDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link ProductAccount} and its DTO {@link ProductAccountDTO}.
 */
@Mapper(componentModel = "spring", uses = {CustomerMapper.class})
public interface ProductAccountMapper extends EntityMapper<ProductAccountDTO, ProductAccount> {

    @Mapping(source = "customer.id", target = "customerId")
    ProductAccountDTO toDto(ProductAccount productAccount);

    @Mapping(source = "customerId", target = "customer")
    ProductAccount toEntity(ProductAccountDTO productAccountDTO);

    default ProductAccount fromId(Long id) {
        if (id == null) {
            return null;
        }
        ProductAccount productAccount = new ProductAccount();
        productAccount.setId(id);
        return productAccount;
    }
}

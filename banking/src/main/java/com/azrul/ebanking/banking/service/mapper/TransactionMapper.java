package com.azrul.ebanking.banking.service.mapper;


import com.azrul.ebanking.banking.domain.*;
import com.azrul.ebanking.banking.service.dto.TransactionDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Transaction} and its DTO {@link TransactionDTO}.
 */
@Mapper(componentModel = "spring", uses = {ProductAccountMapper.class})
public interface TransactionMapper extends EntityMapper<TransactionDTO, Transaction> {

    @Mapping(source = "source.id", target = "sourceId")
    @Mapping(source = "target.id", target = "targetId")
    TransactionDTO toDto(Transaction transaction);

    @Mapping(source = "sourceId", target = "source")
    @Mapping(source = "targetId", target = "target")
    Transaction toEntity(TransactionDTO transactionDTO);

    default Transaction fromId(Long id) {
        if (id == null) {
            return null;
        }
        Transaction transaction = new Transaction();
        transaction.setId(id);
        return transaction;
    }
}

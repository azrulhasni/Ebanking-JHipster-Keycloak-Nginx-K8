package com.azrul.ebanking.banking.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ProductAccountMapperTest {

    private ProductAccountMapper productAccountMapper;

    @BeforeEach
    public void setUp() {
        productAccountMapper = new ProductAccountMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(productAccountMapper.fromId(id).getId()).isEqualTo(id);
        assertThat(productAccountMapper.fromId(null)).isNull();
    }
}

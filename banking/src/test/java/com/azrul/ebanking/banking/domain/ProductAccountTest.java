package com.azrul.ebanking.banking.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import com.azrul.ebanking.banking.web.rest.TestUtil;

public class ProductAccountTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProductAccount.class);
        ProductAccount productAccount1 = new ProductAccount();
        productAccount1.setId(1L);
        ProductAccount productAccount2 = new ProductAccount();
        productAccount2.setId(productAccount1.getId());
        assertThat(productAccount1).isEqualTo(productAccount2);
        productAccount2.setId(2L);
        assertThat(productAccount1).isNotEqualTo(productAccount2);
        productAccount1.setId(null);
        assertThat(productAccount1).isNotEqualTo(productAccount2);
    }
}

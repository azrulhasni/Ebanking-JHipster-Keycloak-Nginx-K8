package com.azrul.ebanking.banking.service.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import com.azrul.ebanking.banking.web.rest.TestUtil;

public class ProductAccountDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProductAccountDTO.class);
        ProductAccountDTO productAccountDTO1 = new ProductAccountDTO();
        productAccountDTO1.setId(1L);
        ProductAccountDTO productAccountDTO2 = new ProductAccountDTO();
        assertThat(productAccountDTO1).isNotEqualTo(productAccountDTO2);
        productAccountDTO2.setId(productAccountDTO1.getId());
        assertThat(productAccountDTO1).isEqualTo(productAccountDTO2);
        productAccountDTO2.setId(2L);
        assertThat(productAccountDTO1).isNotEqualTo(productAccountDTO2);
        productAccountDTO1.setId(null);
        assertThat(productAccountDTO1).isNotEqualTo(productAccountDTO2);
    }
}

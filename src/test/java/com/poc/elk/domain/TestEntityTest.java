package com.poc.elk.domain;

import static com.poc.elk.domain.TestEntityTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.poc.elk.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TestEntityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TestEntity.class);
        TestEntity testEntity1 = getTestEntitySample1();
        TestEntity testEntity2 = new TestEntity();
        assertThat(testEntity1).isNotEqualTo(testEntity2);

        testEntity2.setId(testEntity1.getId());
        assertThat(testEntity1).isEqualTo(testEntity2);

        testEntity2 = getTestEntitySample2();
        assertThat(testEntity1).isNotEqualTo(testEntity2);
    }
}

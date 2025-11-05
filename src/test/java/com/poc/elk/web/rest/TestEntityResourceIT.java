package com.poc.elk.web.rest;

import static com.poc.elk.domain.TestEntityAsserts.*;
import static com.poc.elk.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.elk.IntegrationTest;
import com.poc.elk.domain.TestEntity;
import com.poc.elk.repository.TestEntityRepository;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TestEntityResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TestEntityResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/test-entities";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TestEntityRepository testEntityRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTestEntityMockMvc;

    private TestEntity testEntity;

    private TestEntity insertedTestEntity;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TestEntity createEntity() {
        return new TestEntity().name(DEFAULT_NAME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TestEntity createUpdatedEntity() {
        return new TestEntity().name(UPDATED_NAME);
    }

    @BeforeEach
    void initTest() {
        testEntity = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTestEntity != null) {
            testEntityRepository.delete(insertedTestEntity);
            insertedTestEntity = null;
        }
    }

    @Test
    @Transactional
    void createTestEntity() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TestEntity
        var returnedTestEntity = om.readValue(
            restTestEntityMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(testEntity)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TestEntity.class
        );

        // Validate the TestEntity in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertTestEntityUpdatableFieldsEquals(returnedTestEntity, getPersistedTestEntity(returnedTestEntity));

        insertedTestEntity = returnedTestEntity;
    }

    @Test
    @Transactional
    void createTestEntityWithExistingId() throws Exception {
        // Create the TestEntity with an existing ID
        testEntity.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTestEntityMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(testEntity)))
            .andExpect(status().isBadRequest());

        // Validate the TestEntity in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllTestEntities() throws Exception {
        // Initialize the database
        insertedTestEntity = testEntityRepository.saveAndFlush(testEntity);

        // Get all the testEntityList
        restTestEntityMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(testEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getTestEntity() throws Exception {
        // Initialize the database
        insertedTestEntity = testEntityRepository.saveAndFlush(testEntity);

        // Get the testEntity
        restTestEntityMockMvc
            .perform(get(ENTITY_API_URL_ID, testEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(testEntity.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getNonExistingTestEntity() throws Exception {
        // Get the testEntity
        restTestEntityMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTestEntity() throws Exception {
        // Initialize the database
        insertedTestEntity = testEntityRepository.saveAndFlush(testEntity);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the testEntity
        TestEntity updatedTestEntity = testEntityRepository.findById(testEntity.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTestEntity are not directly saved in db
        em.detach(updatedTestEntity);
        updatedTestEntity.name(UPDATED_NAME);

        restTestEntityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedTestEntity.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedTestEntity))
            )
            .andExpect(status().isOk());

        // Validate the TestEntity in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTestEntityToMatchAllProperties(updatedTestEntity);
    }

    @Test
    @Transactional
    void putNonExistingTestEntity() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        testEntity.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTestEntityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, testEntity.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(testEntity))
            )
            .andExpect(status().isBadRequest());

        // Validate the TestEntity in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTestEntity() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        testEntity.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTestEntityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(testEntity))
            )
            .andExpect(status().isBadRequest());

        // Validate the TestEntity in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTestEntity() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        testEntity.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTestEntityMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(testEntity)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TestEntity in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTestEntityWithPatch() throws Exception {
        // Initialize the database
        insertedTestEntity = testEntityRepository.saveAndFlush(testEntity);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the testEntity using partial update
        TestEntity partialUpdatedTestEntity = new TestEntity();
        partialUpdatedTestEntity.setId(testEntity.getId());

        restTestEntityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTestEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTestEntity))
            )
            .andExpect(status().isOk());

        // Validate the TestEntity in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTestEntityUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTestEntity, testEntity),
            getPersistedTestEntity(testEntity)
        );
    }

    @Test
    @Transactional
    void fullUpdateTestEntityWithPatch() throws Exception {
        // Initialize the database
        insertedTestEntity = testEntityRepository.saveAndFlush(testEntity);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the testEntity using partial update
        TestEntity partialUpdatedTestEntity = new TestEntity();
        partialUpdatedTestEntity.setId(testEntity.getId());

        partialUpdatedTestEntity.name(UPDATED_NAME);

        restTestEntityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTestEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTestEntity))
            )
            .andExpect(status().isOk());

        // Validate the TestEntity in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTestEntityUpdatableFieldsEquals(partialUpdatedTestEntity, getPersistedTestEntity(partialUpdatedTestEntity));
    }

    @Test
    @Transactional
    void patchNonExistingTestEntity() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        testEntity.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTestEntityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, testEntity.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(testEntity))
            )
            .andExpect(status().isBadRequest());

        // Validate the TestEntity in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTestEntity() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        testEntity.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTestEntityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(testEntity))
            )
            .andExpect(status().isBadRequest());

        // Validate the TestEntity in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTestEntity() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        testEntity.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTestEntityMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(testEntity)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TestEntity in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTestEntity() throws Exception {
        // Initialize the database
        insertedTestEntity = testEntityRepository.saveAndFlush(testEntity);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the testEntity
        restTestEntityMockMvc
            .perform(delete(ENTITY_API_URL_ID, testEntity.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return testEntityRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected TestEntity getPersistedTestEntity(TestEntity testEntity) {
        return testEntityRepository.findById(testEntity.getId()).orElseThrow();
    }

    protected void assertPersistedTestEntityToMatchAllProperties(TestEntity expectedTestEntity) {
        assertTestEntityAllPropertiesEquals(expectedTestEntity, getPersistedTestEntity(expectedTestEntity));
    }

    protected void assertPersistedTestEntityToMatchUpdatableProperties(TestEntity expectedTestEntity) {
        assertTestEntityAllUpdatablePropertiesEquals(expectedTestEntity, getPersistedTestEntity(expectedTestEntity));
    }
}

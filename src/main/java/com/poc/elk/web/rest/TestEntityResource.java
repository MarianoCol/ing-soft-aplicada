package com.poc.elk.web.rest;

import com.poc.elk.domain.TestEntity;
import com.poc.elk.repository.TestEntityRepository;
import com.poc.elk.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.poc.elk.domain.TestEntity}.
 */
@RestController
@RequestMapping("/api/test-entities")
@Transactional
public class TestEntityResource {

    private static final Logger LOG = LoggerFactory.getLogger(TestEntityResource.class);

    private static final String ENTITY_NAME = "testEntity";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TestEntityRepository testEntityRepository;

    public TestEntityResource(TestEntityRepository testEntityRepository) {
        this.testEntityRepository = testEntityRepository;
    }

    /**
     * {@code POST  /test-entities} : Create a new testEntity.
     *
     * @param testEntity the testEntity to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new testEntity, or with status {@code 400 (Bad Request)} if the testEntity has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TestEntity> createTestEntity(@RequestBody TestEntity testEntity) throws URISyntaxException {
        LOG.debug("REST request to save TestEntity : {}", testEntity);
        if (testEntity.getId() != null) {
            throw new BadRequestAlertException("A new testEntity cannot already have an ID", ENTITY_NAME, "idexists");
        }
        testEntity = testEntityRepository.save(testEntity);
        return ResponseEntity.created(new URI("/api/test-entities/" + testEntity.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, testEntity.getId().toString()))
            .body(testEntity);
    }

    /**
     * {@code PUT  /test-entities/:id} : Updates an existing testEntity.
     *
     * @param id the id of the testEntity to save.
     * @param testEntity the testEntity to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated testEntity,
     * or with status {@code 400 (Bad Request)} if the testEntity is not valid,
     * or with status {@code 500 (Internal Server Error)} if the testEntity couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TestEntity> updateTestEntity(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody TestEntity testEntity
    ) throws URISyntaxException {
        LOG.debug("REST request to update TestEntity : {}, {}", id, testEntity);
        if (testEntity.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, testEntity.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!testEntityRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        testEntity = testEntityRepository.save(testEntity);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, testEntity.getId().toString()))
            .body(testEntity);
    }

    /**
     * {@code PATCH  /test-entities/:id} : Partial updates given fields of an existing testEntity, field will ignore if it is null
     *
     * @param id the id of the testEntity to save.
     * @param testEntity the testEntity to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated testEntity,
     * or with status {@code 400 (Bad Request)} if the testEntity is not valid,
     * or with status {@code 404 (Not Found)} if the testEntity is not found,
     * or with status {@code 500 (Internal Server Error)} if the testEntity couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TestEntity> partialUpdateTestEntity(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody TestEntity testEntity
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TestEntity partially : {}, {}", id, testEntity);
        if (testEntity.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, testEntity.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!testEntityRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TestEntity> result = testEntityRepository
            .findById(testEntity.getId())
            .map(existingTestEntity -> {
                if (testEntity.getName() != null) {
                    existingTestEntity.setName(testEntity.getName());
                }

                return existingTestEntity;
            })
            .map(testEntityRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, testEntity.getId().toString())
        );
    }

    /**
     * {@code GET  /test-entities} : get all the testEntities.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of testEntities in body.
     */
    @GetMapping("")
    public List<TestEntity> getAllTestEntities() {
        LOG.debug("REST request to get all TestEntities");
        return testEntityRepository.findAll();
    }

    /**
     * {@code GET  /test-entities/:id} : get the "id" testEntity.
     *
     * @param id the id of the testEntity to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the testEntity, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TestEntity> getTestEntity(@PathVariable("id") Long id) {
        LOG.debug("REST request to get TestEntity : {}", id);
        Optional<TestEntity> testEntity = testEntityRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(testEntity);
    }

    /**
     * {@code DELETE  /test-entities/:id} : delete the "id" testEntity.
     *
     * @param id the id of the testEntity to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestEntity(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete TestEntity : {}", id);
        testEntityRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}

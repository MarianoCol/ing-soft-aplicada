package com.poc.elk.repository;

import com.poc.elk.domain.TestEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TestEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TestEntityRepository extends JpaRepository<TestEntity, Long> {}

package com.poc.elk.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TestEntityTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static TestEntity getTestEntitySample1() {
        return new TestEntity().id(1L).name("name1");
    }

    public static TestEntity getTestEntitySample2() {
        return new TestEntity().id(2L).name("name2");
    }

    public static TestEntity getTestEntityRandomSampleGenerator() {
        return new TestEntity().id(longCount.incrementAndGet()).name(UUID.randomUUID().toString());
    }
}

package com.example.shoppingcart;

import com.example.shoppingcart.config.AsyncSyncConfiguration;
import com.example.shoppingcart.config.DatabaseTestcontainer;
import com.example.shoppingcart.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        ShoppingCartApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        com.example.shoppingcart.config.JacksonHibernateConfiguration.class,
    }
)
@ImportTestcontainers(DatabaseTestcontainer.class)
public @interface IntegrationTest {}

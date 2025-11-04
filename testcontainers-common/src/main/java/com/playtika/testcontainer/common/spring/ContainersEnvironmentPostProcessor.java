package com.playtika.testcontainer.common.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.DockerClientFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * EnvironmentPostProcessor for Spring Boot 3 that runs even earlier than ApplicationContextInitializer,
 * during environment preparation phase, before ApplicationContext is created.
 *
 * This processor can be used to:
 * - Check Docker availability very early
 * - Add container-related properties to the environment
 * - Perform early initialization without Spring Cloud bootstrap
 *
 * Usage: Automatically registered via META-INF/spring.factories
 */
@Slf4j
public class ContainersEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "embeddedContainersEnvironment";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Check if embedded containers are enabled
        Boolean containersEnabled = environment.getProperty("embedded.containers.enabled", Boolean.class, true);
        if (!containersEnabled) {
            log.debug("Embedded containers are disabled, skipping Docker availability check");
            return;
        }

        log.debug("EnvironmentPostProcessor: Checking Docker availability during environment preparation...");

        // Check Docker availability
        boolean dockerAvailable = DockerClientFactory.instance().isDockerAvailable();

        // Add Docker availability information to environment
        Map<String, Object> containerProperties = new HashMap<>();
        containerProperties.put("embedded.containers.docker.available", dockerAvailable);

        MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, containerProperties);
        environment.getPropertySources().addLast(propertySource);

        if (dockerAvailable) {
            log.debug("Docker is available for embedded containers");
        } else {
            log.warn("Docker is not available. Container startup will fail unless disabled.");
        }
    }
}

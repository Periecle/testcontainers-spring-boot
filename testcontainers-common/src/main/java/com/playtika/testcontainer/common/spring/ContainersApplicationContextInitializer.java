package com.playtika.testcontainer.common.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.DockerClientFactory;

/**
 * ApplicationContextInitializer for Spring Boot 3 that ensures Docker is available
 * before the ApplicationContext starts, without requiring Spring Cloud bootstrap.
 *
 * This initializer runs at HIGHEST_PRECEDENCE to check Docker availability early
 * in the application lifecycle, before any containers are started.
 *
 * Usage: Automatically registered via META-INF/spring.factories
 */
@Slf4j
public class ContainersApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        // Check if embedded containers are enabled
        Boolean containersEnabled = environment.getProperty("embedded.containers.enabled", Boolean.class, true);
        if (!containersEnabled) {
            log.info("Embedded containers are disabled, skipping Docker presence check");
            return;
        }

        log.debug("Checking Docker availability before ApplicationContext starts...");

        // Check Docker availability early
        boolean dockerAvailable = DockerClientFactory.instance().isDockerAvailable();

        if (!dockerAvailable) {
            throw new DockerNotPresentException(
                "Docker is not available. Please ensure Docker is installed and running. " +
                "If you want to disable embedded containers, set 'embedded.containers.enabled=false'");
        }

        log.debug("Docker is available and ready for container startup");
    }
}

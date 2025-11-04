package com.playtika.testcontainer.common.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.DockerClientFactory;

/**
 * Initializer that checks Docker availability before ApplicationContext starts.
 * Provides an alternative to Spring Cloud bootstrap for Spring Boot 3 applications.
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

        Boolean containersEnabled = environment.getProperty("embedded.containers.enabled", Boolean.class, true);
        if (!containersEnabled) {
            log.debug("Embedded containers are disabled");
            return;
        }

        boolean dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        if (!dockerAvailable) {
            throw new DockerNotPresentException(
                "Docker is not available. Please ensure Docker is installed and running. " +
                "If you want to disable embedded containers, set 'embedded.containers.enabled=false'");
        }

        log.debug("Docker is available");
    }
}

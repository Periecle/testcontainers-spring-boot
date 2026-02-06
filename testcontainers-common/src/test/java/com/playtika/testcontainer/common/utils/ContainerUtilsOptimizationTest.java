package com.playtika.testcontainer.common.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageCmd;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.playtika.testcontainer.common.properties.CommonContainerProperties;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.ImagePullPolicy;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContainerUtilsOptimizationTest {

    private static final Logger log = LoggerFactory.getLogger(ContainerUtilsOptimizationTest.class);

    @Test
    void shouldInspectImageOnlyOnceWithCache() {
        String imageName = "my-image:latest";
        String createdDate = "2023-10-26T12:00:00Z";

        // Mocks
        GenericContainer<?> container = mock(GenericContainer.class);
        DockerClient dockerClient = mock(DockerClient.class);
        InspectImageCmd inspectImageCmd = mock(InspectImageCmd.class);
        InspectImageResponse inspectImageResponse = mock(InspectImageResponse.class);

        // Fluent API mocks for GenericContainer
        when(container.withStartupTimeout(any(Duration.class))).thenReturn((GenericContainer) container);
        when(container.withReuse(anyBoolean())).thenReturn((GenericContainer) container);
        when(container.withLogConsumer(any())).thenReturn((GenericContainer) container);
        when(container.withImagePullPolicy(any(ImagePullPolicy.class))).thenReturn((GenericContainer) container);
        when(container.withEnv(any(Map.class))).thenReturn((GenericContainer) container);
        when(container.withLabels(any(Map.class))).thenReturn((GenericContainer) container);
        // configureCommonsAndStart might call other methods depending on properties, keeping it minimal for now

        // Docker Client Mocks
        when(container.getDockerImageName()).thenReturn(imageName);
        when(container.getDockerClient()).thenReturn(dockerClient);
        when(dockerClient.inspectImageCmd(imageName)).thenReturn(inspectImageCmd);
        when(inspectImageCmd.exec()).thenReturn(inspectImageResponse);
        when(inspectImageResponse.getCreated()).thenReturn(createdDate);

        // Properties
        CommonContainerProperties properties = new CommonContainerProperties() {
            @Override
            public String getDefaultDockerImage() {
                return imageName;
            }
        };
        // Set minimal properties to avoid other branches
        properties.setEnv(Collections.emptyMap());
        properties.setLabel(Collections.emptyMap());
        properties.setWaitTimeoutInSeconds(10);
        properties.setReuseContainer(false);
        properties.setUsePullAlwaysPolicy(false);
        // TmpFs, FilesToInclude, MountVolumes, Capabilities are empty by default or handled

        // Execution 1
        ContainerUtils.configureCommonsAndStart(container, properties, log);

        // Execution 2
        ContainerUtils.configureCommonsAndStart(container, properties, log);

        // Verify optimization: inspectImageCmd called 1 time
        verify(dockerClient, times(1)).inspectImageCmd(imageName);
    }
}

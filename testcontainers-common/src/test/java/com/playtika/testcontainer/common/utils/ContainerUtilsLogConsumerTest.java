package com.playtika.testcontainer.common.utils;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.testcontainers.containers.output.OutputFrame;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class ContainerUtilsLogConsumerTest {

    @Test
    public void verifyFunctionalityWithDebugEnabled() {
        Logger mockLogger = mock(Logger.class);
        when(mockLogger.isDebugEnabled()).thenReturn(true);

        OutputFrame mockFrame = mock(OutputFrame.class);
        when(mockFrame.getType()).thenReturn(OutputFrame.OutputType.STDOUT);
        when(mockFrame.getUtf8String()).thenReturn("test log");

        Consumer<OutputFrame> consumer = ContainerUtils.containerLogsConsumer(mockLogger);
        consumer.accept(mockFrame);

        verify(mockLogger).debug("test log");
    }

    @Test
    public void verifyOptimizationWithDebugDisabled() {
        Logger mockLogger = mock(Logger.class);
        when(mockLogger.isDebugEnabled()).thenReturn(false);

        OutputFrame mockFrame = mock(OutputFrame.class);
        when(mockFrame.getType()).thenReturn(OutputFrame.OutputType.STDOUT);
        when(mockFrame.getUtf8String()).thenReturn("test log");

        Consumer<OutputFrame> consumer = ContainerUtils.containerLogsConsumer(mockLogger);
        consumer.accept(mockFrame);

        // This helps us verify if the optimization is in place.
        // Before optimization, this call happens. After optimization, it should not.
        verify(mockFrame, never()).getUtf8String();
    }
}

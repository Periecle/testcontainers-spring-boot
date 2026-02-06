package com.playtika.testcontainer.common.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TcpPortAvailableUtilsTest {

    @Test
    public void shouldFindAvailableTcpPort() {
        int port = TcpPortAvailableUtils.findAvailableTcpPort();
        assertThat(port).isBetween(TcpPortAvailableUtils.PORT_RANGE_MIN, TcpPortAvailableUtils.PORT_RANGE_MAX);
    }

    @Test
    public void shouldFindAvailableTcpPortInRange() {
        int min = 20000;
        int max = 20100;
        int port = TcpPortAvailableUtils.findAvailableTcpPort(min, max);
        assertThat(port).isBetween(min, max);
    }

    @Test
    public void shouldFindAvailableUdpPort() {
        int port = TcpPortAvailableUtils.findAvailableUdpPort();
        assertThat(port).isBetween(TcpPortAvailableUtils.PORT_RANGE_MIN, TcpPortAvailableUtils.PORT_RANGE_MAX);
    }

    @Test
    public void shouldFindAvailableUdpPortInRange() {
        int min = 20000;
        int max = 20100;
        int port = TcpPortAvailableUtils.findAvailableUdpPort(min, max);
        assertThat(port).isBetween(min, max);
    }
}

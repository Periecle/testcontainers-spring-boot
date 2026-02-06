package com.playtika.testcontainer.common.utils;

import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class TcpPortAvailableUtils {

    public static final int PORT_RANGE_MIN = 1024;

    public static final int PORT_RANGE_MAX = 65535;

    private static final int MAX_SEARCH_ATTEMPTS = 100;

    public static int findAvailableTcpPort() {
        return findAvailableTcpPort(PORT_RANGE_MIN);
    }

    public static int findAvailableTcpPort(int minPort) {
        return findAvailableTcpPort(minPort, PORT_RANGE_MAX);
    }

    public static int findAvailableTcpPort(int minPort, int maxPort) {
        return TcpPortAvailableUtils.SocketType.TCP.findAvailablePort(minPort, maxPort);
    }

    public static int findAvailableUdpPort() {
        return findAvailableUdpPort(PORT_RANGE_MIN);
    }

    public static int findAvailableUdpPort(int minPort) {
        return findAvailableUdpPort(minPort, PORT_RANGE_MAX);
    }

    public static int findAvailableUdpPort(int minPort, int maxPort) {
        return TcpPortAvailableUtils.SocketType.UDP.findAvailablePort(minPort, maxPort);
    }

    private enum SocketType {

        TCP {
            @Override
            protected boolean isPortAvailable(int port) {
                try {
                    ServerSocket serverSocket = new ServerSocket(port, 1, InetAddress.getLoopbackAddress());
                    serverSocket.close();
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }
        },

        UDP {
            @Override
            protected boolean isPortAvailable(int port) {
                try {
                    DatagramSocket socket = new DatagramSocket(port, InetAddress.getLoopbackAddress());
                    socket.close();
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }
        };

        protected abstract boolean isPortAvailable(int port);

        private int findRandomPort(int minPort, int maxPort) {
            int portRange = maxPort - minPort;
            return minPort + ThreadLocalRandom.current().nextInt(portRange + 1);
        }

        int findAvailablePort(int minPort, int maxPort) {
            Assert.isTrue(minPort > 0, "'minPort' must be greater than 0");
            Assert.isTrue(maxPort >= minPort, "'maxPort' must be greater than or equal to 'minPort'");
            Assert.isTrue(maxPort <= PORT_RANGE_MAX, "'maxPort' must be less than or equal to " + PORT_RANGE_MAX);

            int portRange = maxPort - minPort;
            int candidatePort;
            int searchCounter = 0;
            do {
                if (searchCounter > portRange || searchCounter > MAX_SEARCH_ATTEMPTS) {
                    throw new IllegalStateException(String.format(
                            "Could not find an available %s port in the range [%d, %d] after %d attempts",
                            name(), minPort, maxPort, searchCounter));
                }
                candidatePort = findRandomPort(minPort, maxPort);
                searchCounter++;
            }
            while (!isPortAvailable(candidatePort));

            return candidatePort;
        }
    }
}

package mit.spbau.ru.common;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class Seed implements Serializable {
    private final String ip;
    private final int port;

    public Seed(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Seed) {
            return ip.equals(((Seed) other).getIp()) &&
                    port == ((Seed) other).getPort();
        }

        return false;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}

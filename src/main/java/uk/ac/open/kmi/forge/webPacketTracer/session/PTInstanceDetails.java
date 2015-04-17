package uk.ac.open.kmi.forge.webPacketTracer.session;

public class PTInstanceDetails {

    final String host;
    final int port;

    public PTInstanceDetails(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}

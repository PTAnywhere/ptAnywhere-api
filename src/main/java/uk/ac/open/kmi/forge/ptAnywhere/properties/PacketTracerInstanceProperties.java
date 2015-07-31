package uk.ac.open.kmi.forge.ptAnywhere.properties;

public class PacketTracerInstanceProperties {

    String hostname;
    int port;

    public PacketTracerInstanceProperties(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }
}

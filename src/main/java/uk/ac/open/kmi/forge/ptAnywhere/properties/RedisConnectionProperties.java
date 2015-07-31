package uk.ac.open.kmi.forge.ptAnywhere.properties;

public class RedisConnectionProperties {

    String hostname;
    int port;
    int dbNumber;

    public RedisConnectionProperties(String hostname, int port, int dbNumber) {
        this.hostname = hostname;
        this.port = port;
        this.dbNumber = dbNumber;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public int getDbNumber() {
        return dbNumber;
    }
}

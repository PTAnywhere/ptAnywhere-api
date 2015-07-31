package uk.ac.open.kmi.forge.ptAnywhere.session;

public class PTInstanceDetails {

    final String url;
    final String host;
    final int port;

    public PTInstanceDetails(String url, String host, int port) {
        this.url = url;
        this.host = host;
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}

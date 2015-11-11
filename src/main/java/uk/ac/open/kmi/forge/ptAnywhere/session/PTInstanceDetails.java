package uk.ac.open.kmi.forge.ptAnywhere.session;

public class PTInstanceDetails {

    final String url;
    final String host;
    final int port;
    final FileLoadingTask loadingTask;

    public PTInstanceDetails(String url, String host, int port, FileLoadingTask loadingTask) {
        this.url = url;
        this.host = host;
        this.port = port;
        this.loadingTask = loadingTask;
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

    public FileLoadingTask getFileLoadingTask() {
        return loadingTask;
    }
}

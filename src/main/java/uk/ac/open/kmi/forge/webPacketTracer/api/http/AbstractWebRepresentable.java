package uk.ac.open.kmi.forge.webPacketTracer.api.http;


// This class is used to decouple URL generation from the rest of the packages...
public abstract class AbstractWebRepresentable {

    protected URLFactory uf = null;

    public void setURLFactory(URLFactory uf) {
        this.uf = uf;
    }

    // Full URL of the resource.
    // It must also be the main identifier for the HTTP API clients.
    public abstract String getUrl();

    public void setUrl(String url) {
        /* FAKE implementation just to ensure that the POJO is created. */
    }
}

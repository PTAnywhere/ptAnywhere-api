package uk.ac.open.kmi.forge.webPacketTracer.pojo;


import uk.ac.open.kmi.forge.webPacketTracer.api.http.AbstractWebRepresentable;

public class Link extends AbstractWebRepresentable<Link> {
    String id;  // E.g., "cc57bc49d73a42a5aa6a1c78066d565c"
    String toDevice;
    String toPort;

    public Link() {
    }

    public Link(String id, String toDevice, String toPort) {
        this.id = id;
        this.toDevice = toDevice;
        this.toPort = toPort;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getUrl() {
        if (this.uf==null) return null;
        return this.uf.createLinkURL(this.id);
    }

    public String getToDevice() {
        return toDevice;
    }

    public void setToDevice(String toDevice) {
        this.toDevice = toDevice;
    }

    public String getToPort() {
        return toPort;
    }

    public void setToPort(String toPort) {
        this.toPort = toPort;
    }
}

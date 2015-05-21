package uk.ac.open.kmi.forge.webPacketTracer.pojo;


public class Link {
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

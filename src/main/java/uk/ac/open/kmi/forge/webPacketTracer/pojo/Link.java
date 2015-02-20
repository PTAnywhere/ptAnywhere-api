package uk.ac.open.kmi.forge.webPacketTracer.pojo;

public class Link {
    String toDevice;
    String toPort;

    public Link() {
    }

    public Link(String toDevice, String toPort) {
        this.toDevice = toDevice;
        this.toPort = toPort;
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

package uk.ac.open.kmi.forge.webPacketTracer.pojo;

import java.util.*;

public class Network {
    Collection<Device> devices;
    Collection<Edge> edges;

    public Network() {
        this.devices = new HashSet<Device>();
        this.edges = null;
    }

    public Collection<Device> getDevices() {
        return devices;
    }

    public void setDevices(Collection<Device> devices) {
        this.devices = devices;
    }

    public Collection<Edge> getEdges() {
        return edges;
    }

    public void setEdges(Collection<Edge> edges) {
        this.edges = edges;
    }
}

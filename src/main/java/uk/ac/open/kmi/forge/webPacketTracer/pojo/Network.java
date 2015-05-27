package uk.ac.open.kmi.forge.webPacketTracer.pojo;

import uk.ac.open.kmi.forge.webPacketTracer.api.http.URLFactory;

import java.util.*;

public class Network {
    Collection<Device> devices;
    Collection<Edge> edges;

    private URLFactory uf = null;

    public Network() {
        this.devices = new HashSet<Device>();
        this.edges = null;
    }

    public Network setURLFactory(URLFactory uf) {
        this.uf = uf;
        return this;
    }

    public Collection<Device> getDevices() {
        if (this.uf!=null) {
            for(Device d: this.devices) {
                d.setURLFactory(this.uf);
            }
        }
        return this.devices;
    }

    public void setDevices(Collection<Device> devices) {
        this.devices = devices;
    }

    public Collection<Edge> getEdges() {
        if (this.uf!=null) {
            for(Edge e: this.edges) {
                e.setURLFactory(this.uf);
            }
        }
        return edges;
    }

    public void setEdges(Collection<Edge> edges) {
        this.edges = edges;
    }
}

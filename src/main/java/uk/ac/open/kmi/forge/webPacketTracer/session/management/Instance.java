package uk.ac.open.kmi.forge.webPacketTracer.session.management;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Instance {
    int id;
    String url;
    String dockerId;
    String packetTracer;
    String vnc;
    String createdAt;
    String deletedAt;

    public Instance() {
    }

    protected static Instance clone(Instance toClone) {
        return new Instance(toClone.id, toClone.url, toClone.dockerId, toClone.packetTracer, toClone.vnc, toClone.createdAt, toClone.deletedAt);
    }


    public Instance(int id, String url, String dockerId, String packetTracerHost, String vnc, String createdAt, String deletedAt) {
        this.id = id;
        this.dockerId = dockerId;
        this.packetTracer = packetTracerHost;
        this.vnc = vnc;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDockerId() {
        return dockerId;
    }

    public void setDockerId(String dockerId) {
        this.dockerId = dockerId;
    }

    public String getPacketTracer() {
        return packetTracer;
    }

    public String getPacketTracerHostname() {
        return packetTracer.split(":")[0];
    }

    public int getPacketTracerPort() {
        return Integer.valueOf(packetTracer.split(":")[1]);
    }

    public void setPacketTracer(String packetTracer) {
        this.packetTracer = packetTracer;
    }

    public String getVnc() {
        return vnc;
    }

    public void setVnc(String vnc) {
        this.vnc = vnc;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Instance)) return false;

        Instance instance = (Instance) o;

        if (id != instance.id) return false;
        if (url != null ? !url.equals(instance.url) : instance.url != null) return false;
        if (dockerId != null ? !dockerId.equals(instance.dockerId) : instance.dockerId != null) return false;
        if (packetTracer != null ? !packetTracer.equals(instance.packetTracer) : instance.packetTracer != null)
            return false;
        if (vnc != null ? !vnc.equals(instance.vnc) : instance.vnc != null) return false;
        if (createdAt != null ? !createdAt.equals(instance.createdAt) : instance.createdAt != null) return false;
        return !(deletedAt != null ? !deletedAt.equals(instance.deletedAt) : instance.deletedAt != null);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (dockerId != null ? dockerId.hashCode() : 0);
        result = 31 * result + (packetTracer != null ? packetTracer.hashCode() : 0);
        result = 31 * result + (vnc != null ? vnc.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (deletedAt != null ? deletedAt.hashCode() : 0);
        return result;
    }
}
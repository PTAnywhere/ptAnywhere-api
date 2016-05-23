package uk.ac.open.kmi.forge.ptAnywhere.session.management;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Allocation {
    int id;
    String url;
    String packetTracer;
    String createdAt;
    String deletedAt;

    public Allocation() {
    }

    protected static Allocation clone(Allocation toClone) {
        return new Allocation(toClone.id, toClone.url, toClone.packetTracer, toClone.createdAt, toClone.deletedAt);
    }

    public Allocation(int id, String url, String packetTracer, String createdAt, String deletedAt) {
        this.id = id;
        this.packetTracer = packetTracer;
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
        if (!(o instanceof Allocation)) return false;

        Allocation allocation = (Allocation) o;

        if (id != allocation.id) return false;
        if (url != null ? !url.equals(allocation.url) : allocation.url != null) return false;
        if (packetTracer != null ? !packetTracer.equals(allocation.packetTracer) : allocation.packetTracer != null)
            return false;
        if (createdAt != null ? !createdAt.equals(allocation.createdAt) : allocation.createdAt != null) return false;
        return !(deletedAt != null ? !deletedAt.equals(allocation.deletedAt) : allocation.deletedAt != null);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (packetTracer != null ? packetTracer.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (deletedAt != null ? deletedAt.hashCode() : 0);
        return result;
    }
}
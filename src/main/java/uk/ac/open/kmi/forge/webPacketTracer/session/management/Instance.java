package uk.ac.open.kmi.forge.webPacketTracer.session.management;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Instance {
    int id;
    String url;
    String dockerId;
    int pt_port;
    String vnc_port;
    String created_at;
    String deleted_at;

    public Instance() {
    }

    protected static Instance clone(Instance toClone) {
        return new Instance(toClone.id, toClone.url, toClone.dockerId, toClone.pt_port, toClone.vnc_port, toClone.created_at, toClone.deleted_at);
    }


    public Instance(int id, String url, String dockerId, int pt_port, String vnc_port, String created_at, String deleted_at) {
        this.id = id;
        this.dockerId = dockerId;
        this.pt_port = pt_port;
        this.vnc_port = vnc_port;
        this.created_at = created_at;
        this.deleted_at = deleted_at;
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

    public int getPt_port() {
        return pt_port;
    }

    public void setPt_port(int pt_port) {
        this.pt_port = pt_port;
    }

    public String getVnc_port() {
        return vnc_port;
    }

    public void setVnc_port(String vnc_port) {
        this.vnc_port = vnc_port;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(String deleted_at) {
        this.deleted_at = deleted_at;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Instance)) return false;

        Instance instance = (Instance) o;

        if (id != instance.id) return false;
        if (pt_port != instance.pt_port) return false;
        if (dockerId != null ? !dockerId.equals(instance.dockerId) : instance.dockerId != null) return false;
        if (vnc_port != null ? !vnc_port.equals(instance.vnc_port) : instance.vnc_port != null) return false;
        if (created_at != null ? !created_at.equals(instance.created_at) : instance.created_at != null) return false;
        return !(deleted_at != null ? !deleted_at.equals(instance.deleted_at) : instance.deleted_at != null);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (dockerId != null ? dockerId.hashCode() : 0);
        result = 31 * result + pt_port;
        result = 31 * result + (vnc_port != null ? vnc_port.hashCode() : 0);
        result = 31 * result + (created_at != null ? created_at.hashCode() : 0);
        result = 31 * result + (deleted_at != null ? deleted_at.hashCode() : 0);
        return result;
    }
}
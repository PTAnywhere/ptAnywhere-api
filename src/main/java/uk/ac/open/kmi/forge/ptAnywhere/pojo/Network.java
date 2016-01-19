package uk.ac.open.kmi.forge.ptAnywhere.pojo;

import java.util.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory;

@ApiModel(value="Network", description="Information for graphically representing a network.")
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

    @ApiModelProperty(value="Devices which belong to the network", required=true)
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

    @ApiModelProperty(value="Edges which represent links between devices", required=true)
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

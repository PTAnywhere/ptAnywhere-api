package uk.ac.open.kmi.forge.ptAnywhere.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(value="HalfLink", description="Describes a port where another port is connected to.")
public class HalfLink {
    String toPortURL;

    public HalfLink() {
    }

    public HalfLink(String toPortURL) {
        this.toPortURL = toPortURL;
    }

    @ApiModelProperty(value="URL of the port to which another port is connected to", required=true)
    public String getToPort() {
        return toPortURL;
    }

    public void setToPort(String toPort) {
        this.toPortURL = toPort;
    }
}

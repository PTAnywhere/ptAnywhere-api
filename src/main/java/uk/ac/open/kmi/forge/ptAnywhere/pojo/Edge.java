package uk.ac.open.kmi.forge.ptAnywhere.pojo;

import javax.xml.bind.annotation.XmlType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.AbstractWebRepresentable;


/**
 * Class specifically designed for vis.js.
 * It is used when the network map is loaded.
 */
@XmlType(name="")
@ApiModel(value="Edge", description="Short way to describe the connection between two devices for the graphical representation of the network.")
public class Edge extends AbstractWebRepresentable<Edge> {
    String id;  // E.g., a9101f6bef7c437291c29391e94ee233
    String from;  // E.g., 4e70e5d74399485eb4096c9d1c9446ea
    String to;  // E.g., 6fc7797b1a334fd78db11d6e7468db65
    String fromLabel;
    String toLabel;

    public Edge() {
    }

    public Edge(String id, String from, String fromLabel) {
        this.id = id;
        this.from = from;
        this.fromLabel = fromLabel;
    }

    @ApiModelProperty(value="Identifier of the edge", required=true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(value="URL of the link that this edge represents", required=true)
    @Override
    public String getUrl() {
        if (this.uf==null) return null;
        return this.uf.createLinkURL(this.id);
    }

    @ApiModelProperty(value="Identifier of one of the two devices that this edge connects", required=true)
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @ApiModelProperty(value="Identifier of one of the two devices that this edge connects", required=true)
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @ApiModelProperty(value="Name of one of the ports that this edge connects", required=true)
    public String getFromLabel() {
        return fromLabel;
    }

    public void setFromLabel(String fromLabel) {
        this.fromLabel = fromLabel;
    }

    @ApiModelProperty(value="Name of one of the ports that this edge connects", required=true)
    public String getToLabel() {
        return toLabel;
    }

    public void setToLabel(String toLabel) {
        this.toLabel = toLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;

        Edge edge = (Edge) o;

        if (from != null ? !from.equals(edge.from) : edge.from != null) return false;
        if (id != null ? !id.equals(edge.id) : edge.id != null) return false;
        if (to != null ? !to.equals(edge.to) : edge.to != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "id='" + id + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }
}

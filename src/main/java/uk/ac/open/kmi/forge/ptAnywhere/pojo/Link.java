package uk.ac.open.kmi.forge.ptAnywhere.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.AbstractWebRepresentable;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory;

import javax.xml.bind.annotation.XmlType;


@XmlType(name="")
@ApiModel(value="Link", description="Connection between two ports.")
public class Link extends AbstractWebRepresentable<Link> {
    String id;  // E.g., "{cc57bc49-d73a-42a5-aa6a-1c78066d565c}"
    String[] endpoints;

    public Link() {
        this.endpoints = new String[2];
    }

    public static Link createFromInnerLink(InnerLink il, URLFactory uf) {
        final Link rl = new Link();
        rl.setId(il.getId());
        rl.setURLFactory(uf); // To generate URL
        String[] endpoints = new String[2];
        for(int i=0; i<endpoints.length; i++) {
            endpoints[i] = uf.createPortURL(il.getDeviceId(i), il.getPortName(i));
        }
        rl.setEndpoints(endpoints);
        return rl;
    }

    @ApiModelProperty(value="Identifier of the link", required=true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(value="URL which identifies this link")
    @Override
    public String getUrl() {
        if (this.uf==null) return null;
        return this.uf.createLinkURL(this.id);
    }

    @ApiModelProperty(value="URLs of the two ports that this link connects", required=true)
    public String[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String[] endpoints) {
        this.endpoints = endpoints;
    }
}

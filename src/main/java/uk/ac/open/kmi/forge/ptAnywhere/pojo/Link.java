package uk.ac.open.kmi.forge.ptAnywhere.pojo;

import uk.ac.open.kmi.forge.ptAnywhere.api.http.AbstractWebRepresentable;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory;


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
            String[] endpoint = il.getEndpoints()[i];
            endpoints[i] = uf.createPortURL(endpoint[0], endpoint[1]);
        }
        rl.setEndpoints(endpoints);
        return rl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getUrl() {
        if (this.uf==null) return null;
        return this.uf.createLinkURL(this.id);
    }

    public String[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String[] endpoints) {
        this.endpoints = endpoints;
    }
}

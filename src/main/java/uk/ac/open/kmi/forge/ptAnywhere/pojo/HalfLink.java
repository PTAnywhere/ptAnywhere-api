package uk.ac.open.kmi.forge.ptAnywhere.pojo;


import uk.ac.open.kmi.forge.ptAnywhere.api.http.AbstractWebRepresentable;

public class HalfLink extends AbstractWebRepresentable<HalfLink> {
    String id;  // E.g., "cc57bc49d73a42a5aa6a1c78066d565c"
    String toPortURL;

    public HalfLink() {
    }

    public HalfLink(String id, String toPortURL) {
        this.id = id;
        this.toPortURL = toPortURL;
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

    public String getToPort() {
        return toPortURL;
    }

    public void setToPort(String toPort) {
        this.toPortURL = toPort;
    }
}

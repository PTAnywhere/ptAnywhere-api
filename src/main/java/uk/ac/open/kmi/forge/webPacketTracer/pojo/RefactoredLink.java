package uk.ac.open.kmi.forge.webPacketTracer.pojo;

public class RefactoredLink {
    String id;  // E.g., "{cc57bc49-d73a-42a5-aa6a-1c78066d565c}"
    String[] endpoints;

    public RefactoredLink() {
        this.endpoints = new String[2];
    }

    public RefactoredLink(String id, String end1, String end2) {
        this.id = id;
        this.endpoints = new String[] {end1, end2};
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String[] endpoints) {
        this.endpoints = endpoints;
    }

    public void appendEndpoint(String endpointURL) {
        if (this.endpoints[0]==null)
            this.endpoints[0] = endpointURL;
        else
            this.endpoints[1] = endpointURL;
    }
}

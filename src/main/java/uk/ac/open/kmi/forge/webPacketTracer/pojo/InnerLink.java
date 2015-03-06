package uk.ac.open.kmi.forge.webPacketTracer.pojo;

public class InnerLink {
    final String id;  // E.g., "{cc57bc49-d73a-42a5-aa6a-1c78066d565c}"
    final String[][] endpoints;

    public InnerLink(String id) {
        this.id = id;
        this.endpoints = new String[2][2];
    }

    public void appendEndpoint(String deviceId, String portId) {
        final int pos = getNextEmptyPosition();  // If the first one is already set, put the second one.
        this.endpoints[pos][0] = deviceId;
        this.endpoints[pos][1] = portId;
    }

    private int getNextEmptyPosition() {
        return (isEndpointSet(0))? 1: 0;  // If the first one is already set, put the second one.
    }

    private boolean isEndpointSet(int pos) {
        return this.endpoints[pos][1]!=null; // any of them
    }

    public String getId() {
        return id;
    }

    public String[][] getEndpoints() {
        return endpoints;
    }

    public boolean areEndpointsSet() {
        return isEndpointSet(1); // If the last one is set...
    }
}

package uk.ac.open.kmi.forge.webPacketTracer.pojo;

import uk.ac.open.kmi.forge.webPacketTracer.api.http.Utils;

public class InnerLink {
    final String id;  //
    final String[][] endpoints;

    /**
     * @param id
     *      E.g., cc57bc49d73a42a5aa6a1c78066d565c
     */
    public InnerLink(String id) {
        this.id = id;
        this.endpoints = new String[2][2];
    }

    public void appendEndpoint(String deviceCiscoId, String portName) {
        final int pos = getNextEmptyPosition();  // If the first one is already set, put the second one.
        this.endpoints[pos][0] = Utils.toSimplifiedUUID(deviceCiscoId);
        this.endpoints[pos][1] = portName;
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

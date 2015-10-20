package uk.ac.open.kmi.forge.ptAnywhere.pojo;


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

    /**
     * Add endpoint to link only if it does not already exist.
     * @param deviceId
     * @param portName
     */
    public void appendEndpoint(String deviceId, String portName) {
        if (!isAlreadyAdded(deviceId, portName)) {
            final int pos = getNextEmptyPosition();  // If the first one is already set, put the second one.
            this.endpoints[pos][0] = deviceId;
            this.endpoints[pos][1] = portName;
        }
    }

    private boolean isAlreadyAdded(String deviceId, String portName) {
        for (String[] endpoint: this.endpoints) {
            if (deviceId.equals(endpoint[0]) && portName.equals(endpoint[1])) return true;
        }
        return false;
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

    public String getDeviceId(int endpointNumber) {
        if (endpointNumber < this.endpoints.length)
            return this.endpoints[endpointNumber][0];
        return null;
    }

    public String getPortName(int endpointNumber) {
        if (endpointNumber<this.endpoints.length)
            return this.endpoints[endpointNumber][1];
        return null;
    }

    public boolean areEndpointsSet() {
        return isEndpointSet(1); // If the last one is set...
    }
}

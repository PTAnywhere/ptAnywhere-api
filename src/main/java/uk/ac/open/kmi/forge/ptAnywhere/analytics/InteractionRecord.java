package uk.ac.open.kmi.forge.ptAnywhere.analytics;

import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;


public abstract class InteractionRecord {
    public void setURIFactory(URIFactory factory) {}
    public void setSession(String sessionId) {}
    public void setIdentity(Identifiable identity) {}
    public void interactionStarted() {}
    public void deviceCreated(String deviceUri, String deviceName, String deviceType, double x, double y) {}
    public void deviceDeleted(String deviceUri, String deviceName, String deviceType) {}
    public void deviceModified(String deviceUri, String deviceName, String deviceType, String newDeviceName) {}
    public void deviceModified(String deviceUri, String deviceName, String deviceType, String newDeviceName, String defaultGateway) {}
    public void portModified(String portUri, String deviceName, String portName, String ipAddress, String subnetMask) {}
    public void deviceConnected(String linkUri, String endpoint1Name, String endpoint1Port, String endpoint2Name, String endpoint2Port) {}
    public void deviceDisconnected(String linkUri, String endpoint1Name, String endpoint1Port, String endpoint2Name, String endpoint2Port) {}
    public void commandLineStarted(String deviceUri) {}
    public void commandLineUsed(String deviceUri, String input) {}
    public void commandLineRead(String deviceUri, String output) {}
    public void commandLineEnded(String deviceUri) {}
}
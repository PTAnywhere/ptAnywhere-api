package uk.ac.open.kmi.forge.ptAnywhere.analytics;


public abstract class InteractionRecord {
    public void setURIFactory(URIFactory factory) {}
    public void setSession(String sessionId) {}
    public void interactionStarted() {}
    public void deviceCreated(String deviceUri, String deviceName, String deviceType) {}
    public void deviceDeleted(String deviceUri, String deviceName, String deviceType) {}
    public void deviceModified(String deviceUri, String deviceName, String deviceType) {}
    public void deviceConnected(String linkUri, String[] endpointURLs) {}
    public void deviceDisconnected(String linkUri, String[] endpointURLs) {}
    public void commandLineStarted(String deviceUri) {}
    public void commandLineUsed(String deviceUri, String input) {}
    public void commandLineEnded(String deviceUri) {}
}

package uk.ac.open.kmi.forge.ptAnywhere.analytics;


public abstract class InteractionRecord {
    public void interactionStarted(String sessionId) {}
    public void deviceCreated(String sessionId, String deviceUri, String deviceName, String deviceType) {}
    public void deviceDeleted(String sessionId, String deviceUri, String deviceName, String deviceType) {}
    public void deviceModified(String sessionId, String deviceUri, String deviceName, String deviceType) {}
    public void deviceConnected(String sessionId, String linkUri, String[] endpointURLs) {}
    public void deviceDisconnected(String sessionId, String linkUri, String[] endpointURLs) {}
    public void commandLineStarted(String sessionId, String deviceUri) {}
    public void commandLineUsed(String sessionId, String deviceUri, String input) {}
    public void commandLineEnded(String sessionId, String deviceUri) {}
}

package uk.ac.open.kmi.forge.webPacketTracer.analytics;


public abstract class InteractionRecord {
    public void interactionStarted(String sessionId) {}
    public void deviceCreated(String sessionId, String deviceUri, String deviceName, String deviceType) {}
    public void deviceDeleted(String sessionId, String deviceUri, String deviceName, String deviceType) {}
    public void deviceModified(String sessionId, String deviceUri, String deviceName, String deviceType) {}
}

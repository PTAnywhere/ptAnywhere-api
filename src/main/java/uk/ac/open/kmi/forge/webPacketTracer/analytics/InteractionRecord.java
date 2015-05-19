package uk.ac.open.kmi.forge.webPacketTracer.analytics;

import java.net.URI;

public abstract class InteractionRecord {
    public void interactionStarted(String sessionId) {}
    public void deviceCreated(String sessionId, String deviceUri) {}
}

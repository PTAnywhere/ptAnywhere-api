package uk.ac.open.kmi.forge.webPacketTracer.session;

public interface ExpirationSubscriber extends Runnable {
    public void stop();
}
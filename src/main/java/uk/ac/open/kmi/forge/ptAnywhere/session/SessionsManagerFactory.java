package uk.ac.open.kmi.forge.ptAnywhere.session;


public interface SessionsManagerFactory {
    SessionsManager create();
    ExpirationSubscriber createExpirationSubscription();
}

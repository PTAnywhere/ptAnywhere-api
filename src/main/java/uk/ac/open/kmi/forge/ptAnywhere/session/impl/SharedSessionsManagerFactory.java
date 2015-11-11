package uk.ac.open.kmi.forge.ptAnywhere.session.impl;

import java.util.HashSet;
import java.util.Set;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.NoPTInstanceAvailableException;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PacketTracerInstanceProperties;
import uk.ac.open.kmi.forge.ptAnywhere.session.ExpirationSubscriber;
import uk.ac.open.kmi.forge.ptAnywhere.session.PTInstanceDetails;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManager;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManagerFactory;


public class SharedSessionsManagerFactory implements SessionsManagerFactory {

    final String SESSION_ID = "kmiPacketTracerSession0--";
    final PTInstanceDetails uniqueInstance;

    protected SharedSessionsManagerFactory(PacketTracerInstanceProperties instanceDetails) {
        this.uniqueInstance = new PTInstanceDetails(null, instanceDetails.getHostname(), instanceDetails.getPort(), null);
    }

    public SessionsManager create() {
        return new SharedSessionsManager();
    }

    public ExpirationSubscriber createExpirationSubscription() {
        return null;
    }

    /**
     * This manager uses an existing shared and precreated PT instance for all the sessions.
     *
     * This will only be used temporarily (in the production server) until the other SessionsManager is stable enough.
     */
    class SharedSessionsManager implements SessionsManager {
        public void clear() {}

        public void addManagementAPIs(String... apiUrls) {}

        public String createSession(String inputFileUrl) throws NoPTInstanceAvailableException {
            // WARNING: No file opening will be allowed in this mode: inputFileUrl is ignored.
            return SESSION_ID;  // Only one session
        }

        public Set<String> getCurrentSessions() {
            final Set<String> ret = new HashSet<String>();
            ret.add(createSession(null));
            return ret;
        }

        public PTInstanceDetails getInstance(String sessionId) {
            return uniqueInstance;
        }

        public boolean doesExist(String sessionId) {
            return SESSION_ID.equals(sessionId);
        }

        public void deleteSession(String sessionId) {}

        /* Methods to ease webapp management */
        public Set<PTInstanceDetails> getAllInstances() {
            final Set<PTInstanceDetails> ret = new HashSet<PTInstanceDetails>();
            ret.add(uniqueInstance);
            return ret;
        }
    }
}
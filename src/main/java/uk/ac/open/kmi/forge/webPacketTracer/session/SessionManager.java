package uk.ac.open.kmi.forge.webPacketTracer.session;

import uk.ac.open.kmi.forge.webPacketTracer.api.http.exceptions.SessionNotFoundException;

/**
 * SessionsManager with a default sessionId.
 */
public class SessionManager {

    final String sessionId;
    final SessionsManager sm;

    public SessionManager(String sessionId, SessionsManager sm) {
        this.sessionId = sessionId;
        this.sm = sm;
    }

    public PTInstanceDetails getInstance() throws SessionNotFoundException {
        final PTInstanceDetails details = this.sm.getInstance(this.sessionId);
        // FIXME Mixing layers => It's a presentation exception :-S
        if (details==null) throw new SessionNotFoundException(this.sessionId);
        return details;
    }

    public String getSessionId() {
        return this.sessionId;
    }
}

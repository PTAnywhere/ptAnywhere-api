package uk.ac.open.kmi.forge.ptAnywhere.analytics;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.tincanapi.OnePerRegistrationRecorder;
import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;
import uk.ac.open.kmi.forge.ptAnywhere.identity.factories.AnonymousIdentityFactory;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.IdentityFinder;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical.BySessionId;
import uk.ac.open.kmi.forge.ptAnywhere.properties.InteractionRecordingProperties;


public class InteractionRecordFactory {

    private static final Log LOGGER = LogFactory.getLog(InteractionRecordFactory.class);

    private final ExecutorService executor;  // This executor is not handled by this class.
    private final InteractionRecordingProperties irp;

    private OnePerRegistrationRecorder recorder;
    final private IdentityFinder idFinder;


    public InteractionRecordFactory(ExecutorService executor, InteractionRecordingProperties props, IdentityFinder idFinder) {
        this.executor = executor;
        this.irp = props;
        this.idFinder = idFinder;
        this.recorder = null;
    }

    protected InteractionRecord createBasic() {
        if (this.irp==null) return new NoTracker();
        try {
            if (this.recorder==null) {
                this.recorder = new OnePerRegistrationRecorder(this.irp.getEndpoint(), this.irp.getUsername(), this.irp.getPassword(), this.executor);  // Shared among TinCanAPI objects
            }
            return new TinCanAPI(this.recorder);
        } catch(MalformedURLException e) {
            LOGGER.error(e.getMessage());
            return new NoTracker();
        }
    }

    // TODO Big FIXME!!!! Rethink this!

    /**
     * It creates an interaction record for a previously existing session.
     * @param widgetURI
     *      URI that identifies the widget which is using the API in this interaction.
     * @param sessionId
     *      The session id of the new session.
     * @return
     */
    public InteractionRecord create(String widgetURI, String sessionId) {
        final InteractionRecord ir = createBasic();
        ir.setURIFactory(new URIFactory(widgetURI));
        ir.setSession(sessionId);
        final Identifiable identity;
        if (this.idFinder==null) {
            identity = new AnonymousIdentityFactory(sessionId).create();
        } else {
            identity = this.idFinder.findIdentity(new BySessionId(sessionId));
        }
        ir.setIdentity(identity);
        return ir;
    }

    /**
     * It creates an interaction record for a new session.
     * @param widgetURI
     *      URI that identifies the widget which is using the API in this interaction.
     * @param sessionId
     *      The session id of the new session.
     * @param previousSessionId
     *      The ID of a previous session that same user created.
     *      It can be null if it is unknown or it is the first session of this user.
     * @return
     */
    public InteractionRecord createForNewSession(String widgetURI, String sessionId, String previousSessionId) {
        final InteractionRecord ir = createBasic();
        ir.setURIFactory(new URIFactory(widgetURI));
        ir.setSession(sessionId);
        final Identifiable identity;
        if (this.idFinder==null) {
            identity = new AnonymousIdentityFactory(sessionId).create();
        } else {
            if (previousSessionId==null) {
                // If no previous session id is passed, let's create a new anonymous user.
                identity = new AnonymousIdentityFactory(sessionId).create();
            } else {
                identity = this.idFinder.findIdentity(new BySessionId(previousSessionId), new AnonymousIdentityFactory(sessionId));
            }
        }
        ir.setIdentity(identity);
        return ir;
    }
}
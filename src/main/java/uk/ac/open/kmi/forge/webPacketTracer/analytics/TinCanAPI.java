package uk.ac.open.kmi.forge.webPacketTracer.analytics;

import com.rusticisoftware.tincan.*;
import com.rusticisoftware.tincan.lrsresponses.StatementLRSResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.webPacketTracer.api.http.Utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;


/**
 * Interaction recording in TinCan API.
 * To describe interactions, vocabulary from this registry [1] has been used by default.
 *
 * [1] https://registry.tincanapi.com
 */
public class TinCanAPI extends InteractionRecord {

    private static final Log LOGGER = LogFactory.getLog(TinCanAPI.class);

    // Own vocabulary
    // TODO get it from the HTTP API requester?
    private static final String WIDGET = "http://ict-forge.eu/widget/packerTracer";

    /* Verbs */
    private static final String INITIALIZED = "http://adlnet.gov/expapi/verbs/initialized";
    private static final String CREATED = "http://activitystrea.ms/schema/1.0/create";
    private static final String DELETED = "http://activitystrea.ms/schema/1.0/delete";
    private static final String UPDATED = "http://activitystrea.ms/schema/1.0/update";

    /* Objects */
    private static final String DEVICE_TYPE = WIDGET + "/devices/type/";

    /* Extensions */
    private static final String ENDPOINTS = WIDGET + "/endpoints";

    final RemoteLRS lrs = new RemoteLRS();
    final ExecutorService executor;

    protected TinCanAPI(String endpoint, String username, String password, ExecutorService executor) throws MalformedURLException {
        this.executor = executor;
        this.lrs.setEndpoint(endpoint);
        this.lrs.setVersion(TCAPIVersion.V100);
        this.lrs.setUsername(username);
        this.lrs.setPassword(password);
    }

    private void record(final Statement statement) {
        final Runnable saveTask = new Runnable() {
            @Override
            public void run() {
                final StatementLRSResponse lrsRes = lrs.saveStatement(statement);
                if (lrsRes.getSuccess()) {
                    // success, use lrsRes.getContent() to get the statement back
                    LOGGER.debug("Everything went ok.");
                } else {
                    // failure, error information is available in lrsRes.getErrMsg()
                    LOGGER.error("Something went wrong recording the sentence.");
                }
            }
        };
        // To avoid adding uneeded delays in the HTTP request which is recording
        // the statement, we do it in a different Thread...
        this.executor.submit(saveTask);
    }

    private Agent getAnonymousUser(String sessionId) {
        final AgentAccount aa = new AgentAccount();
        // This could be set to the real URL where the app is deployed.
        aa.setHomePage("http://forge.kmi.open.ac.uk/pt/widget");
        aa.setName("anonymous_" + sessionId);
        Agent agent = new Agent();
        agent.setAccount(aa);
        return agent;
    }

    /*
    Right now, this information is unnecessary as the user can be used
    for reporting sessions.
    However, in the future user might be de-anonymized.
     */
    private Context getContext(String sessionId) {
        final Context context = new Context();
        context.setRegistration(Utils.toUUID(sessionId));
        return context;
    }

    public Statement getPrefilledStatement(String sessionId) {
        final Statement st = new Statement();
        st.setActor(getAnonymousUser(sessionId));
        st.setContext(getContext(sessionId));
        return st;
    }

    public void interactionStarted(String sessionId) {
        try {
            final Statement st = getPrefilledStatement(sessionId);
            st.setVerb(new Verb(INITIALIZED));
            st.setObject(new Activity(WIDGET));
            record(st);
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }


    private Activity createDeviceObject(String deviceUri, String deviceName) throws URISyntaxException {
        return createDeviceObject(deviceUri, deviceName, null);
    }

    private Activity createDeviceObject(String deviceUri, String deviceName, String deviceType) throws URISyntaxException {
        final LanguageMap lm = new LanguageMap();
        lm.put("en-GB", deviceName);
        final ActivityDefinition definition = new ActivityDefinition();
        if (deviceType!=null)
            definition.setType(DEVICE_TYPE + deviceType);
        definition.setName(lm);
        final Activity ret = new Activity(deviceUri);
        ret.setDefinition(definition);
        return ret;
    }

    public void deviceCreated(String sessionId, String deviceUri, String deviceName, String deviceType) {
        try {
            final Statement st = getPrefilledStatement(sessionId);
            st.setVerb(new Verb(CREATED));
            st.setObject(createDeviceObject(deviceUri, deviceName, deviceType));

            record(st);
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void deviceDeleted(String sessionId, String deviceUri, String deviceName, String deviceType) {
        try {
            final Statement st = getPrefilledStatement(sessionId);
            st.setVerb(new Verb(DELETED));
            st.setObject(createDeviceObject(deviceUri, deviceName, deviceType));

            record(st);
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void deviceModified(String sessionId, String deviceUri, String deviceName, String deviceType) {
        try {
            final Statement st = getPrefilledStatement(sessionId);
            st.setVerb(new Verb(UPDATED));
            st.setObject(createDeviceObject(deviceUri, deviceName, deviceType));

            record(st);
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private Activity createLinkObject(String linkUri) throws URISyntaxException {
        final LanguageMap lm = new LanguageMap();
        lm.put("en-GB", "link"); // Generic name defined to enhance readability in LearningLocker.
        final ActivityDefinition definition = new ActivityDefinition();
        definition.setName(lm);
        final Activity ret = new Activity(linkUri);
        ret.setDefinition(definition);
        return ret;
    }

    /*
     * Alternatives to model a connection:
     *      - user creates link1 (extensions: endpoints: [portUrl1, portUrl2]
     *      - user connects device1 (extension: port: portUrl1, with: portUrl2)
     *      - previous alternative x2 (one for each endpoint)
     *
     * I will use the first one as it seems more symmetric and I don't invent new verbs (connect).
     */
    public void deviceConnected(String sessionId, String linkUri, String[] endpointURLs) {
        try {
            final Statement st = getPrefilledStatement(sessionId);
            st.setVerb(new Verb(CREATED));
            st.setObject( createLinkObject(linkUri) );
            final Extensions ext = new Extensions();
            ext.put(new URI(ENDPOINTS), endpointURLs);
            st.getContext().setExtensions(ext);

            record(st);
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void deviceDisconnected(String sessionId, String linkUri, String[] endpointURLs) {
        try {
            final Statement st = getPrefilledStatement(sessionId);
            st.setVerb(new Verb(DELETED));
            st.setObject( createLinkObject(linkUri) );
            final Extensions ext = new Extensions();
            ext.put(new URI(ENDPOINTS), endpointURLs);
            st.getContext().setExtensions(ext);

            record(st);
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
package uk.ac.open.kmi.forge.ptAnywhere.analytics;

import com.rusticisoftware.tincan.*;
import com.rusticisoftware.tincan.lrsresponses.StatementLRSResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.Utils;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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
    private static final String VOCAB = "http://ict-forge.eu/vocab";

    // Activity http://adlnet.gov/expapi/activities/simulation

    /* Verbs */
    private static final String INITIALIZED = "http://adlnet.gov/expapi/verbs/initialized";
    private static final String TERMINATED = "http://adlnet.gov/expapi/verbs/terminated";
    private static final String CREATED = "http://activitystrea.ms/schema/1.0/create";
    private static final String DELETED = "http://activitystrea.ms/schema/1.0/delete";
    private static final String UPDATED = "http://activitystrea.ms/schema/1.0/update";
    // For command line, we could also register: "open" or "close"
    private static final String USED = "http://activitystrea.ms/schema/1.0/use";

    /* Objects */
    // TODO get it from the HTTP API requester?
    // So far only one activity, but for each widget a new one should be created!
    private static final String WIDGET = "http://ict-forge.eu/packerTracer";  // default
    private static final String DEFAULT_WIDGET = WIDGET + "/default";  // default
    private static final String DEVICE_TYPE = WIDGET + "/devices/type/";
    /** Objects -> Actitivies **/
    private static final String SIMULATION = "http://adlnet.gov/expapi/activities/simulation";
    private static final String ACTIVITIES = VOCAB + "/activities";
    private static final String COMMAND_LINE = ACTIVITIES + "/command-line";

    /* Extensions */
    private static final String EXTENSION = VOCAB + "/extensions";
    private static final String EXT_ENDPOINTS = EXTENSION + "/endpoints";
    private static final String EXT_DEVICE = EXTENSION + "/device";  // Device whose command line we use

    final RemoteLRS lrs = new RemoteLRS();
    final ExecutorService executor;

    private String widgetURI = DEFAULT_WIDGET;  // It can be overriden in setWidget
    private String sessionId;


    protected TinCanAPI(String endpoint, String username, String password, ExecutorService executor) throws MalformedURLException {
        this.executor = executor;
        this.lrs.setEndpoint(endpoint);
        this.lrs.setVersion(TCAPIVersion.V100);
        this.lrs.setUsername(username);
        this.lrs.setPassword(password);
    }

    public void setWidget(String widgetURI) {
        if (widgetURI!=null)
            this.widgetURI = widgetURI;
    }

    public void setSession(String sessionId) {
        this.sessionId = sessionId;
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
                    LOGGER.error("    HTTP error: " + lrsRes.getResponse().getStatusMsg());
                    LOGGER.error("    HTTP response: " + lrsRes.getResponse().getContent());
                    //LOGGER.error(statement.toJSON());
                }
            }
        };
        // To avoid adding uneeded delays in the HTTP request which is recording
        // the statement, we do it in a different Thread...
        this.executor.submit(saveTask);
    }

    private Agent getAnonymousUser() {
        final AgentAccount aa = new AgentAccount();
        // This could be set to the real URL where the app is deployed.
        aa.setHomePage("http://forge.kmi.open.ac.uk/pt/widget");
        aa.setName("anonymous_" + this.sessionId);
        Agent agent = new Agent();
        agent.setAccount(aa);
        return agent;
    }

    private Activity getWidgetActivity() throws URISyntaxException {
        final Activity ret = new Activity(new URI(this.widgetURI));
        final ActivityDefinition definition = new ActivityDefinition();
        definition.setType(SIMULATION);
        ret.setDefinition(definition);
        return ret;
    }

    /*
    Right now, this information is unnecessary as the user can be used
    for reporting sessions.
    However, in the future user might be de-anonymized.
     */
    private Context getContext() {
        final Context context = new Context();
        context.setRegistration(Utils.toUUID(this.sessionId));
        return context;
    }

    private Context addParentActivity(Context context) throws URISyntaxException {
        final List<Activity> parents = new ArrayList<Activity>();
        parents.add(getWidgetActivity());
        final ContextActivities ca = new ContextActivities();
        ca.setParent(parents);
        context.setContextActivities(ca);
        return context;
    }

    public Statement getPrefilledStatement() {
        final Statement st = new Statement();
        st.setActor(getAnonymousUser());
        st.setContext(getContext());
        return st;
    }

    public void interactionStarted() {
        try {
            final Statement st = getPrefilledStatement();
            st.setVerb(new Verb(INITIALIZED));
            st.setObject(getWidgetActivity());
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
        // FIXME Should we reuse the same activity type and put deviceUri as extension?
        final Activity ret = new Activity(deviceUri);
        ret.setDefinition(definition);
        return ret;
    }

    public void deviceCreated(String deviceUri, String deviceName, String deviceType) {
        try {
            final Statement st = getPrefilledStatement();
            st.setVerb(new Verb(CREATED));
            st.setObject(createDeviceObject(deviceUri, deviceName, deviceType));

            record(st);
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void deviceDeleted(String deviceUri, String deviceName, String deviceType) {
        try {
            final Statement st = getPrefilledStatement();
            st.setVerb(new Verb(DELETED));
            st.setObject(createDeviceObject(deviceUri, deviceName, deviceType));

            record(st);
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void deviceModified(String deviceUri, String deviceName, String deviceType) {
        try {
            final Statement st = getPrefilledStatement();
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
    public void deviceConnected(String linkUri, String[] endpointURLs) {
        try {
            final Statement st = getPrefilledStatement();
            st.setVerb(new Verb(CREATED));
            st.setObject( createLinkObject(linkUri) );
            final Extensions ext = new Extensions();
            ext.put(EXT_ENDPOINTS, endpointURLs);
            st.getContext().setExtensions(ext);

            record(st);
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void deviceDisconnected(String linkUri, String[] endpointURLs) {
        try {
            final Statement st = getPrefilledStatement();
            st.setVerb(new Verb(DELETED));
            st.setObject( createLinkObject(linkUri) );
            final Extensions ext = new Extensions();
            ext.put(new URI(EXT_ENDPOINTS), endpointURLs);
            st.getContext().setExtensions(ext);

            record(st);
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private Activity createCommandLineObject(String deviceUri, String deviceName) throws URISyntaxException, IllegalArgumentException,
            UriBuilderException {
        final LanguageMap lm = new LanguageMap();
        lm.put("en-GB", deviceName + "'s command line ");
        final ActivityDefinition definition = new ActivityDefinition();
        definition.setName(lm);
        definition.setType(COMMAND_LINE);
        // It would be particularly useful to refer to activity types across sessions
        //   (e.g., opening X console were X is always the same id)
        final URI clUri = UriBuilder.fromPath(deviceUri).path("console").build();
        final Activity ret = new Activity(clUri);
        ret.setDefinition(definition);
        return ret;
    }

    protected String getDeviceURI(String deviceName) {
        final URIFactory uf = new URIFactory(this.widgetURI);
        return uf.getDeviceURI(deviceName);
    }

    protected Statement createCommandLine(String deviceName, String verb) throws URISyntaxException {
        final Statement st = getPrefilledStatement();
        st.setVerb(new Verb(verb));
        final String deviceUri = getDeviceURI(deviceName);
        st.setObject(createCommandLineObject(deviceUri, deviceName));
        // Note from docs:
        //  "A Statement defined entirely by its extensions becomes meaningless as no other system can make sense of it."
        final Extensions ext = new Extensions();
        ext.put(EXT_DEVICE, deviceUri);
        st.getContext().setExtensions(ext);
        addParentActivity(st.getContext());
        return st;
    }

    public void commandLineStarted(String deviceName) {
        try {
            record( createCommandLine(deviceName, INITIALIZED) );
        } catch(URISyntaxException | IllegalArgumentException | UriBuilderException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void commandLineUsed(String deviceName, String input) {
        try {
            final Statement st = createCommandLine(deviceName, USED);
            final Result result = new Result();
            result.setResponse(input);
            st.setResult(result);

            record(st);
        } catch(URISyntaxException | IllegalArgumentException | UriBuilderException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void commandLineEnded(String deviceName) {
        try {
            record( createCommandLine(deviceName, TERMINATED) );
        } catch(URISyntaxException | IllegalArgumentException | UriBuilderException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
package uk.ac.open.kmi.forge.webPacketTracer.analytics;

import com.rusticisoftware.tincan.*;
import com.rusticisoftware.tincan.lrsresponses.StatementLRSResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.UUID;


/**
 * Interaction recording in TinCan API.
 * To describe interactions, vocabulary from this registry [1] has been used by default.
 *
 * [1] https://registry.tincanapi.com
 */
public class TinCanAPI extends InteractionRecord {

    private static final Log LOGGER = LogFactory.getLog(TinCanAPI.class);

    private static final String INITIALIZED = "http://adlnet.gov/expapi/verbs/initialized";
    private static final String WIDGET = "http://ict-forge.eu/widget/packerTracer";

    final RemoteLRS lrs = new RemoteLRS();

    protected TinCanAPI(String endpoint, String username, String password) throws MalformedURLException {
        this.lrs.setEndpoint(endpoint);
        this.lrs.setVersion(TCAPIVersion.V100);
        this.lrs.setUsername(username);
        this.lrs.setPassword(password);
    }

    private void logRecordingError() {
        LOGGER.error("Something went wrong recording the sentence.");
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
        context.setRegistration(UUID.fromString(sessionId));
        return context;
    }

    public void interactionStarted(String sessionId) {
        try {
            final Statement st = new Statement();
            st.setActor(getAnonymousUser(sessionId));
            st.setVerb(new Verb(INITIALIZED));
            st.setObject(new Activity(WIDGET));
            st.setContext(getContext(sessionId));

            final StatementLRSResponse lrsRes = lrs.saveStatement(st);
            if (lrsRes.getSuccess()) {
                // success, use lrsRes.getContent() to get the statement back
                LOGGER.debug("Everything went ok.");
            } else {
                // failure, error information is available in lrsRes.getErrMsg()
                logRecordingError();
            }
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }


}

package uk.ac.open.kmi.forge.webPacketTracer.analytics;

import com.rusticisoftware.tincan.*;
import com.rusticisoftware.tincan.lrsresponses.StatementLRSResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;


public class TinCanAPI implements InteractionRecordable {

    private static final Log LOGGER = LogFactory.getLog(TinCanAPI.class);

    private static final String INITIALIZED = "http://adlnet.gov/expapi/verbs/initialized";
    private static final String WIDGET = "http://ict-forge.eu/widget/packerTracer";
    private static final String DEFAULT_USER = "mailto:user@ict-forge.eu";

    RemoteLRS lrs = new RemoteLRS();

    public TinCanAPI(String endpoint, String username, String password) throws MalformedURLException {
        this.lrs = new RemoteLRS();
        this.lrs.setEndpoint(endpoint);
        this.lrs.setVersion(TCAPIVersion.V100);
        this.lrs.setUsername(username);
        this.lrs.setPassword(password);
    }

    public void interactionStarted() {
        try {
            Agent agent = new Agent();
            agent.setMbox(DEFAULT_USER);

            Verb verb = new Verb(INITIALIZED);
            Activity activity = new Activity(WIDGET);

            Statement st = new Statement();
            st.setActor(agent);
            st.setVerb(verb);
            st.setObject(activity);

            StatementLRSResponse lrsRes = lrs.saveStatement(st);
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

    private void logRecordingError() {
        LOGGER.error("Something went wrong recording the sentence.");
    }
}

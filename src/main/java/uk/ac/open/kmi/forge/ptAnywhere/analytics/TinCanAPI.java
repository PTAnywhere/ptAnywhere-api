package uk.ac.open.kmi.forge.ptAnywhere.analytics;

import com.rusticisoftware.tincan.*;
import com.rusticisoftware.tincan.lrsresponses.StatementLRSResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab.*;
import javax.ws.rs.core.UriBuilderException;
import java.net.MalformedURLException;
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

    final RemoteLRS lrs = new RemoteLRS();
    final ExecutorService executor;

    private URIFactory factory;
    private String sessionId;


    // For testing
    protected TinCanAPI() {
        this.executor = null;
    }

    protected TinCanAPI(String endpoint, String username, String password, ExecutorService executor) throws MalformedURLException {
        this.executor = executor;
        this.lrs.setEndpoint(endpoint);
        this.lrs.setVersion(TCAPIVersion.V100);
        this.lrs.setUsername(username);
        this.lrs.setPassword(password);
    }

    public void setSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setURIFactory(URIFactory factory) {
        this.factory = factory;
    }

    protected void record(final Statement statement) {
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

    public void interactionStarted() {
        try {
            final StatementBuilder builder = new StatementBuilder(this.factory).
                                                    anonymousUser(this.sessionId).verb(BaseVocabulary.INITIALIZED);
            builder.getActivityBuilder().widgetActivity();
            builder.getContextBuilder().addSession(this.sessionId);
            record(builder.build() );
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void deviceCreated(String deviceUri, String deviceName, String deviceType) {
        try {
            final StatementBuilder builder = new StatementBuilder(this.factory).
                    anonymousUser(this.sessionId).verb(BaseVocabulary.CREATED);
            builder.getActivityBuilder().simulatedDevice(deviceType);
            builder.getContextBuilder().addSession(this.sessionId).addParentActivity();
            builder.getResultBuilder().response(deviceName).
                    deviceNameExt(deviceName).deviceTypeExt(deviceType).deviceURIExt(deviceUri);
            record(builder.build());
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void deviceDeleted(String deviceUri, String deviceName, String deviceType) {
        try {
            final StatementBuilder builder = new StatementBuilder(this.factory).
                    anonymousUser(this.sessionId).verb(BaseVocabulary.DELETED);
            builder.getActivityBuilder().simulatedDevice(deviceType);
            builder.getContextBuilder().addSession(this.sessionId).addParentActivity();
            builder.getResultBuilder().response(deviceName).
                    deviceNameExt(deviceName).deviceTypeExt(deviceType).deviceURIExt(deviceUri);
            record(builder.build());
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void deviceModified(String deviceUri, String deviceName, String deviceType) {
        try {
            final StatementBuilder builder = new StatementBuilder(this.factory).
                    anonymousUser(this.sessionId).verb(BaseVocabulary.UPDATED);
            builder.getActivityBuilder().simulatedDevice(deviceType);
            builder.getContextBuilder().addSession(this.sessionId).addParentActivity();
            builder.getResultBuilder().response(deviceName).
                    deviceNameExt(deviceName).deviceTypeExt(deviceType).deviceURIExt(deviceUri);
            record(builder.build());
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
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
            final StatementBuilder builder = new StatementBuilder(this.factory).
                    anonymousUser(this.sessionId).verb(BaseVocabulary.CREATED);
            builder.getActivityBuilder().connectionActivity();
            builder.getContextBuilder().addSession(this.sessionId).addParentActivity();
            builder.getResultBuilder().response(linkUri).linkUriExt(linkUri);  // TODO check if it makes sense or not
            builder.getResultBuilder().endpointsExt(endpointURLs);
            record(builder.build());
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void deviceDisconnected(String linkUri, String[] endpointURLs) {
        try {
            final StatementBuilder builder = new StatementBuilder(this.factory).
                    anonymousUser(this.sessionId).verb(BaseVocabulary.DELETED);
            builder.getActivityBuilder().connectionActivity();
            builder.getContextBuilder().addSession(this.sessionId).addParentActivity();
            builder.getResultBuilder().response(linkUri).linkUriExt(linkUri);  // TODO check if it makes sense or not
            builder.getResultBuilder().endpointsExt(endpointURLs);
            record(builder.build());
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    protected StatementBuilder createCommandLine(String deviceName, String verb) throws URISyntaxException {
        final StatementBuilder builder = new StatementBuilder(this.factory).
                anonymousUser(this.sessionId).verb(verb);
        builder.getContextBuilder().addSession(this.sessionId).addParentActivity();
        builder.getActivityBuilder().commandLineActivity(deviceName);
        return builder;
    }

    public void commandLineStarted(String deviceName) {
        try {
            record( createCommandLine(deviceName, BaseVocabulary.INITIALIZED).build() );
        } catch(URISyntaxException | IllegalArgumentException | UriBuilderException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void commandLineUsed(String deviceName, String input) {
        try {
            final StatementBuilder builder = createCommandLine(deviceName, BaseVocabulary.USED);
            builder.getResultBuilder().response(input).deviceNameExt(deviceName);
            record(builder.build());
        } catch(URISyntaxException | IllegalArgumentException | UriBuilderException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void commandLineEnded(String deviceName) {
        try {
            record( createCommandLine(deviceName, BaseVocabulary.TERMINATED).build() );
        } catch(URISyntaxException | IllegalArgumentException | UriBuilderException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
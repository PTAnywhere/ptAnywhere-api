package uk.ac.open.kmi.forge.ptAnywhere.analytics;

import com.rusticisoftware.tincan.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.ws.rs.core.UriBuilderException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import uk.ac.open.kmi.forge.ptAnywhere.analytics.tincanapi.StatementRecorder;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab.*;
import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;


/**
 * Interaction recording in TinCan API.
 * To describe interactions, vocabulary from this registry [1] has been used by default.
 *
 * [1] https://registry.tincanapi.com
 */
public class TinCanAPI extends InteractionRecord {

    private static final Log LOGGER = LogFactory.getLog(TinCanAPI.class);

    private final StatementRecorder recorder;
    private URIFactory factory = null;
    private String sessionId = null;
    private Identifiable identity = null;


    // For testing
    protected TinCanAPI() {
        this.recorder = null;
    }

    // Constructor used by the factory
    protected TinCanAPI(StatementRecorder recorder) throws MalformedURLException {
        this.recorder = recorder;
    }

    public void setSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setURIFactory(URIFactory factory) {
        this.factory = factory;
    }

    public void setIdentity(Identifiable identity) {
        this.identity = identity;
    }

    private void record(Statement statement) {
        this.recorder.record(statement);
    }

    protected StatementBuilder getStatementBuilder() {
        return new StatementBuilder(this.factory).student(this.identity);
    }

    @Override
    public void interactionStarted() {
        try {
            final StatementBuilder builder = getStatementBuilder().verb(BaseVocabulary.INITIALIZED);
            builder.getActivityBuilder().widgetActivity();
            builder.getContextBuilder().addSession(this.sessionId);
            record(builder.build() );
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void deviceCreated(String deviceUri, String deviceName, String deviceType, double x, double y) {
        try {
            final StatementBuilder builder = getStatementBuilder().verb(BaseVocabulary.CREATED);
            builder.getActivityBuilder().simulatedDevice(deviceType);
            builder.getContextBuilder().addSession(this.sessionId).addParentActivity();
            builder.getResultBuilder().response(deviceName).
                    deviceNameExt(deviceName).deviceTypeExt(deviceType).
                    positionExt(x, y).deviceURIExt(deviceUri);
            record(builder.build());
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void deviceDeleted(String deviceUri, String deviceName, String deviceType) {
        try {
            final StatementBuilder builder = getStatementBuilder().verb(BaseVocabulary.DELETED);
            builder.getActivityBuilder().simulatedDevice(deviceType);
            builder.getContextBuilder().addSession(this.sessionId).addParentActivity();
            builder.getResultBuilder().response(deviceName).
                    deviceNameExt(deviceName).deviceTypeExt(deviceType).deviceURIExt(deviceUri);
            record(builder.build());
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void deviceModified(String deviceUri, String deviceName, String deviceType, String newDeviceName) {
        deviceModified(deviceUri, deviceName, deviceType, newDeviceName, null);
    }

    @Override
    public void deviceModified(String deviceUri, String deviceName, String deviceType, String newDeviceName, String defaultGateway) {
        try {
            final StatementBuilder builder = getStatementBuilder().verb(BaseVocabulary.UPDATED);
            builder.getActivityBuilder().simulatedDevice(deviceType);
            builder.getContextBuilder().addSession(this.sessionId).addParentActivity();
            builder.getResultBuilder().response(newDeviceName).
                    deviceNameExt(deviceName).deviceTypeExt(deviceType).deviceURIExt(deviceUri);
            if (defaultGateway!=null) {
                builder.getResultBuilder().deviceDefaultGwExt(defaultGateway);
            }
            record(builder.build());
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void portModified(String portUri, String deviceName, String portName, String ipAddress, String subnetMask) {
        try {
            final StatementBuilder builder = getStatementBuilder().verb(BaseVocabulary.UPDATED);
            builder.getActivityBuilder().simulatedPort(deviceName, portName);
            builder.getContextBuilder().addSession(this.sessionId).addParentActivity();
            builder.getResultBuilder().response(portName).
                    deviceNameExt(deviceName).portURIExt(portUri).portNameExt(portName).
                    portIpAddressExt(ipAddress).portSubnetMaskExt(subnetMask);
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
    @Override
    public void deviceConnected(String linkUri, String endpoint1Name, String endpoint1Port, String endpoint2Name, String endpoint2Port) {
        try {
            final StatementBuilder builder = getStatementBuilder().verb(BaseVocabulary.CREATED);
            builder.getActivityBuilder().simulatedLink();
            builder.getContextBuilder().addSession(this.sessionId).addParentActivity();
            builder.getResultBuilder().response(linkUri).linkUriExt(linkUri);  // TODO check if it makes sense or not
            builder.getResultBuilder().endpointsExt(endpoint1Name, endpoint1Port, endpoint2Name, endpoint2Port);
            record(builder.build());
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void deviceDisconnected(String linkUri, String endpoint1Name, String endpoint1Port, String endpoint2Name, String endpoint2Port) {
        try {
            final StatementBuilder builder = getStatementBuilder().verb(BaseVocabulary.DELETED);
            builder.getActivityBuilder().simulatedLink();
            builder.getContextBuilder().addSession(this.sessionId).addParentActivity();
            builder.getResultBuilder().response(linkUri).linkUriExt(linkUri);  // TODO check if it makes sense or not
            builder.getResultBuilder().endpointsExt(endpoint1Name, endpoint1Port, endpoint2Name, endpoint2Port);
            record(builder.build());
        } catch(URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    protected StatementBuilder createCommandLine(String deviceName, String verb) throws URISyntaxException {
        final StatementBuilder builder = getStatementBuilder().verb(verb);
        builder.getContextBuilder().addSession(this.sessionId).addParentActivity();
        builder.getActivityBuilder().commandLineActivity(deviceName);
        return builder;
    }

    @Override
    public void commandLineStarted(String deviceName) {
        try {
            record( createCommandLine(deviceName, BaseVocabulary.OPENED).build() );
        } catch(URISyntaxException | IllegalArgumentException | UriBuilderException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void commandLineUsed(String deviceName, String input) {
        try {
            final StatementBuilder builder = createCommandLine(deviceName, BaseVocabulary.USED);
            builder.getResultBuilder().response(input).deviceNameExt(deviceName);
            final Statement stmt = builder.build();
            record(stmt);
        } catch(URISyntaxException | IllegalArgumentException | UriBuilderException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void commandLineRead(String deviceName, String output) {
        try {
            final StatementBuilder builder = createCommandLine(deviceName, BaseVocabulary.READ);
            builder.getResultBuilder().response(output).deviceNameExt(deviceName);
            final Statement stmt = builder.build();
            record(stmt);
        } catch(URISyntaxException | IllegalArgumentException | UriBuilderException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void commandLineEnded(String deviceName) {
        try {
            record( createCommandLine(deviceName, BaseVocabulary.CLOSED).build() );
        } catch(URISyntaxException | IllegalArgumentException | UriBuilderException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
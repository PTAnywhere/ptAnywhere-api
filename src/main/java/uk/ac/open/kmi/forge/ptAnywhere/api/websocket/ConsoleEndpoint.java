package uk.ac.open.kmi.forge.ptAnywhere.api.websocket;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import com.cisco.pt.ipc.enums.DeviceType;
import com.cisco.pt.ipc.events.TerminalLineEvent;
import com.cisco.pt.ipc.events.TerminalLineEventListener;
import com.cisco.pt.ipc.events.TerminalLineEventRegistry;
import com.cisco.pt.ipc.sim.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecord;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecordFactory;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.Utils;
import uk.ac.open.kmi.forge.ptAnywhere.gateway.PTConnection;
import uk.ac.open.kmi.forge.ptAnywhere.session.PTInstanceDetails;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManagerFactory;


@ServerEndpoint(value = "/endpoint/sessions/{session}/devices/{device}/console",
                encoders = { OutputWritterEventEncoder.class, HistoryEncoder.class, PromptEncoder.class }
)
// Apparently it creates a class for each session + device
public class ConsoleEndpoint implements TerminalLineEventListener {

    private final static Log LOGGER = LogFactory.getLog(ConsoleEndpoint.class);

    // "lrsFactory" is a weak reference because it is handled by the APIApplication class.
    // TODO better way to access it directly in the APIApplication (which is the manager).
    private static WeakReference<InteractionRecordFactory> lrsFactory;
    private static WeakReference<SessionsManagerFactory> sessionsManagerFactory;

    PTConnection common;
    TerminalLine cmd;
    Session session;

    private String widgetURI;
    private String deviceName = null;

    public ConsoleEndpoint() {}

    private void registerWidgetURI(Session session) {
        this.widgetURI = null; // Default value
        if ( session.getRequestParameterMap().containsKey("widget") ) {
            final List<String> params = session.getRequestParameterMap().get("widget");
            if (!params.isEmpty()) {
                this.widgetURI = params.get(0);
            }
        }
    }

    public static void setInteractionRecordFactory(InteractionRecordFactory irf) {
        ConsoleEndpoint.lrsFactory = new WeakReference<InteractionRecordFactory>(irf);
    }

    public static void setSessionsManagerFactory(SessionsManagerFactory smFactory) {
        ConsoleEndpoint.sessionsManagerFactory = new WeakReference<SessionsManagerFactory>(smFactory);
    }

    private InteractionRecord createInteractionRecordSession(String widgetURI, String sessionId) {
        final InteractionRecordFactory irf = ConsoleEndpoint.lrsFactory.get();
        if (irf==null) return null;
        return ConsoleEndpoint.lrsFactory.get().create(widgetURI, sessionId);
    }

    private String getSessionId(Session session) {
        return session.getPathParameters().get("session");
    }

    private String getDeviceId(Session session) {
        return session.getPathParameters().get("device");
    }

    private String getDeviceName(Session session) {
        if (this.deviceName==null) {
            final String deviceId = getDeviceId(session);
            this.deviceName = this.common.getDataAccessObject().getDeviceById(deviceId).getLabel();
        }
        return this.deviceName;
    }

    @OnOpen
    public void myOnOpen(final Session session) throws IOException, EncodeException {
        registerWidgetURI(session);

        this.session = session;  // Important, the first thing

        final InteractionRecordFactory irf = ConsoleEndpoint.lrsFactory.get();
        final PTInstanceDetails details = ConsoleEndpoint.sessionsManagerFactory.get().create().getInstance(getSessionId(session));
        if (details==null) {
            session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY,
                                            "The session does not exist, it possibly expired."));
        } else {
            this.common = PTConnection.createPacketTracerGateway(details.getHost(), details.getPort());
            this.common.open();
            final String deviceId = getDeviceId(session);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Opening communication channel for device " + deviceId + "'s command line.");
            }
            final Device dev = this.common.getDataAccessObject().getSimDeviceById(Utils.toCiscoUUID(deviceId));
            if (dev == null) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "The device does not exist."));
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Device with id " + deviceId + " not found.");
                }
            } else {
                if (DeviceType.PC.equals(dev.getType()) || DeviceType.SWITCH.equals(dev.getType()) ||
                        DeviceType.ROUTER.equals(dev.getType())) {
                    if (DeviceType.PC.equals(dev.getType())) {
                        this.cmd = ((Pc) dev).getCommandLine();
                    } else {
                        this.cmd = ((CiscoDevice) dev).getConsoleLine();
                    }
                    try {
                        final TerminalLineEventRegistry registry = this.common.getTerminalLineEventRegistry();
                        registry.addListener(this, this.cmd);
                        registerActivityStart(session);
                        if (this.cmd.getPrompt().equals("")) {
                            // Switches and router need a "RETURN" to get started.
                            // Here, we free the client from doing this task.
                            enterCommand(session, "", false);
                        } else {
                            this.session.getBasicRemote().sendObject(this.cmd.getPrompt());
                        }
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                } else {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Console could not opened for device type " + dev.getType().name() + ".");
                    }
                }
            }
        }
    }

    @OnClose
    public void myOnClose(final CloseReason reason) {
        try {
            this.common.getTerminalLineEventRegistry().removeListener(this, this.cmd);
            registerActivityEnd(this.session);
        } catch(IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            this.common.close();
        }
    }

    private void enterCommand(Session session, String msg, boolean last) throws IOException {
        if (session.isOpen()) {
            final String sessionId = session.getPathParameters().get("session");
            if (ConsoleEndpoint.sessionsManagerFactory.get().create().doesExist(sessionId)) {
                this.cmd.enterCommand(msg);
            } else {
                session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "The session has expired."));
            }
        }
    }

    private void registerActivityStart(Session session) {
        final InteractionRecord ir = createInteractionRecordSession(this.widgetURI, getSessionId(session));
        if (ir==null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("No interaction record.");
            }
        } else {
            ir.commandLineStarted(getDeviceName(session));
        }
    }

    private void registerInteraction(Session session, String msg) {
        final InteractionRecord ir = createInteractionRecordSession(this.widgetURI, getSessionId(session));
        if (ir==null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("No interaction record.");
            }
        } else {
            ir.commandLineUsed(getDeviceName(session), msg);
        }
    }

    private void registerActivityEnd(Session session) {
        final InteractionRecord ir = createInteractionRecordSession(this.widgetURI, getSessionId(session));
        if (ir==null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("No interaction record.");
            }
        } else {
            ir.commandLineEnded(getDeviceName(session));
        }
    }

    @OnMessage
    public void typeCommand(Session session, String msg, boolean last) throws IOException, EncodeException {
        // register it in Tin Can API
        if (msg.equals("/getHistory")) {
            session.getBasicRemote().sendObject(this.cmd.getUserHistory().getHistory());
            // TODO register history check activity
        } else {
            if (LOGGER.isInfoEnabled() && msg.endsWith("\t")) {
                msg = msg.trim() + "\t";
                LOGGER.info("Autocompleting command.");
            } else {
                LOGGER.info("Running normal command.");
            }
            enterCommand(session, msg, last);
            registerInteraction(session, msg);
        }
    }

    public void handleEvent(TerminalLineEvent event) throws EncodeException {
        // Maybe we could use other events: commandAutoCompleted, promptChanged, terminalUpdated, etc.
        if (event.eventName.equals("outputWritten")) {
            try {
                this.session.getBasicRemote().sendObject((TerminalLineEvent.OutputWritten) event);
            } catch(IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
package uk.ac.open.kmi.forge.webPacketTracer.api.websocket;

import com.cisco.pt.ipc.enums.DeviceType;
import com.cisco.pt.ipc.events.TerminalLineEvent;
import com.cisco.pt.ipc.events.TerminalLineEventListener;
import com.cisco.pt.ipc.events.TerminalLineEventRegistry;
import com.cisco.pt.ipc.sim.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.webPacketTracer.api.http.Utils;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTConnection;
import uk.ac.open.kmi.forge.webPacketTracer.session.PTInstanceDetails;
import uk.ac.open.kmi.forge.webPacketTracer.session.SessionsManager;

import java.io.IOException;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/endpoint/sessions/{session}/devices/{device}/console")
public class ConsoleEndpoint implements TerminalLineEventListener {

    private static Log logger = LogFactory.getLog(ConsoleEndpoint.class);
    PTConnection common;
    TerminalLine cmd;
    Session session;

    public ConsoleEndpoint() {}

    @OnOpen
    public void myOnOpen(final Session session) {
        this.session = session;

        final String sessionId = session.getPathParameters().get("session");
        final PTInstanceDetails details = SessionsManager.create().getInstance(sessionId);
        if (details==null) return; // Is it better to throw an exception?

        this.common = PTConnection.createPacketTracerGateway(details.getHost(), details.getPort());
        this.common.open();
        final String deviceId = session.getPathParameters().get("device");
        if (logger.isInfoEnabled()) {
            logger.info("Opening communication channel for device " + deviceId + "'s command line.");
        }
        final Device dev = this.common.getDataAccessObject().getSimDeviceById(Utils.toCiscoUUID(deviceId));
        if (dev==null) {
            if(logger.isErrorEnabled()) {
                logger.error("Device with id " + deviceId + " not found." );
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
                    if (this.cmd.getPrompt().equals("")) {
                        // Switches and router need a "RETURN" to get started.
                        // Here, we free the client from doing this task.
                        typeCommand(session, "", false);
                    } else
                        this.session.getBasicRemote().sendText(this.cmd.getPrompt());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                if(logger.isErrorEnabled()) {
                    logger.error("Console could not opened for device type " + dev.getType().name() + ".");
                }
            }
        }
    }

    @OnClose
    public void myOnClose(final CloseReason reason) {
        try {
            //System.out.println("Closing a WebSocket due to " + reason.getReasonPhrase());
            this.common.getTerminalLineEventRegistry().removeListener(this, this.cmd);
        } catch(IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            this.common.close();
        }
    }

    @OnMessage
    public void typeCommand(Session session, String msg, boolean last) {
        if (session.isOpen()) {
            final String sessionId = session.getPathParameters().get("session");
            if (SessionsManager.create().doesExist(sessionId)) {
                this.cmd.enterCommand(msg);
            } else {
                // The current session no longer has access to the PT instance it was using...
                final TerminalLineEventRegistry registry = this.common.getTerminalLineEventRegistry();
                try {
                    registry.removeListener(this);
                    session.getBasicRemote().sendText("\n\n\nThis command line does no longer accept commands.");
                    session.getBasicRemote().sendText("\nYour session might have expired.");
                    session.close();
                } catch(IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void handleEvent(TerminalLineEvent event) {
        if (event.eventName.equals("outputWritten")) {
            try {
                final String msg = ((TerminalLineEvent.OutputWritten) event).newOutput;
                this.session.getBasicRemote().sendText(msg);
            } catch(IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
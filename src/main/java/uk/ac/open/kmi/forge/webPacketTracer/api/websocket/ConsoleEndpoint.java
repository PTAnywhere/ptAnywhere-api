package uk.ac.open.kmi.forge.webPacketTracer.api.websocket;

import com.cisco.pt.ipc.events.TerminalLineEvent;
import com.cisco.pt.ipc.events.TerminalLineEventListener;
import com.cisco.pt.ipc.events.TerminalLineEventRegistry;
import com.cisco.pt.ipc.sim.Pc;
import com.cisco.pt.ipc.sim.TerminalLine;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTConnection;
import java.io.IOException;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/endpoint/devices/{device}/console")
public class ConsoleEndpoint implements TerminalLineEventListener {

    PTConnection common;
    TerminalLine cmd;
    Session session;

    public ConsoleEndpoint() {
        this.common = new PTConnection();
    }

    @OnOpen
    public void myOnOpen(final Session session) {
        this.session = session;

        this.common.open();
        final String deviceId = "{" + session.getPathParameters().get("device") + "}";
        final Pc pc0 = (Pc) this.common.getDataAccessObject().getSimDeviceById(deviceId);
        this.cmd = pc0.getCommandLine();
        try {
            final TerminalLineEventRegistry registry = this.common.getTerminalLineEventRegistry();
            this.session.getBasicRemote().sendText(this.cmd.getPrompt());
            registry.addListener(this, this.cmd);
        } catch(IOException e) {
            this.common.getLog().error(e.getMessage(), e);
        }
    }

    @OnClose
    public void myOnClose(final CloseReason reason) {
        try {
            //System.out.println("Closing a WebSocket due to " + reason.getReasonPhrase());
            this.common.getTerminalLineEventRegistry().removeListener(this, this.cmd);
        } catch(IOException e) {
            this.common.getLog().error(e.getMessage(), e);
        } finally {
            this.common.close();
        }
    }

    @OnMessage
    public void typeCommand(Session session, String msg, boolean last) {
        if (session.isOpen()) {
            this.cmd.enterCommand(msg);
        }
    }

    public void handleEvent(TerminalLineEvent event) {
        if (event.eventName.equals("outputWritten")) {
            try {
                final String msg = ((TerminalLineEvent.OutputWritten) event).newOutput;
                this.session.getBasicRemote().sendText(msg);
            } catch(IOException e) {
                this.common.getLog().error(e.getMessage(), e);
            }
        }
    }
}
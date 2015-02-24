package uk.ac.open.kmi.forge.webPacketTracer.gateway;

import com.cisco.pt.ipc.IPCError;
import com.cisco.pt.ipc.IPCFactory;
import com.cisco.pt.ipc.events.TerminalLineEventRegistry;
import com.cisco.pt.ipc.sim.Network;

/**
 * Class which communicated with PacketTracer but needs to be manually started and stoped (possibly by different threads).
 */
public class PTDaemon extends PTCommon {
    public void start() {
        //System.out.println ("WebSocket opened: "+session.getId());
        try {
            this.before();
        } catch (IPCError ipcError) {
            this.getLog().error("\n\n\nAn IPC error occurred:\n\t" + ipcError.getMessage() + "\n\n\n");
        } catch (Throwable t) {
            if ((t instanceof ThreadDeath)) {
                throw ((ThreadDeath) t);
            }
            this.getLog().error(t);
        }
    }

    public void stop() {
        //System.out.println("Closing a WebSocket due to " + reason.getReasonPhrase());
        try {
            this.after();
        } catch (Throwable t) {
            if ((t instanceof ThreadDeath)) {
                throw ((ThreadDeath) t);
            }
            this.getLog().error(t);
        }
    }

    public Network getNetwork() {
        return this.ipcFactory.network(this.getIPC());
    }

    public TerminalLineEventRegistry getTerminalLineEventRegistry() {
        return this.packetTracerSession.getEventManager().getTerminalLineEvents();
    }
}

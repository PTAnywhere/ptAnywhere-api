package uk.ac.open.kmi.forge.webPacketTracer.gateway;

import com.cisco.pt.ipc.IPCError;
import org.apache.commons.logging.Log;

public abstract class PTRunnable implements Runnable {

    final protected PTConnection connection;

    public PTRunnable() {
        this.connection = new PTConnection();
    }

    public PTRunnable(String hostName, int port) {
        this.connection = new PTConnection(hostName, port);
    }

    @Override
    public void run() {
        try {
            this.connection.before();
            internalRun();
        } catch (IPCError ipcError) {
            this.connection.getLog().error("\n\n\nAn IPC error occurred:\n\t" + ipcError.getMessage() + "\n\n\n");
        } catch (Throwable t) {
            if ((t instanceof ThreadDeath)) {
                throw ((ThreadDeath) t);
            }
            this.connection.getLog().error(t);
        } finally {
            this.connection.close();
        }
    }

    public Log getLog() {
        return this.connection.getLog();
    }

    public abstract void internalRun();
}

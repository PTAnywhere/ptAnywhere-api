package uk.ac.open.kmi.forge.webPacketTracer.gateway;

import com.cisco.pt.ipc.IPCError;
import org.apache.commons.logging.Log;

public abstract class PTRunnable implements Runnable {

    final protected PTCommon task;

    public PTRunnable() {
        this.task = new PTCommon();
    }

    public PTRunnable(String hostName, int port) {
        this.task = new PTCommon(hostName, port);
    }

    @Override
    public void run() {
        try {
            this.task.before();
            internalRun();
        } catch (IPCError ipcError) {
            this.task.getLog().error("\n\n\nAn IPC error occurred:\n\t" + ipcError.getMessage() + "\n\n\n");
        } catch (Throwable t) {
            if ((t instanceof ThreadDeath)) {
                throw ((ThreadDeath) t);
            }
            this.task.getLog().error(t);
        } finally {
            try {
                this.task.after();
            } catch (Throwable t) {
                if ((t instanceof ThreadDeath)) {
                    throw ((ThreadDeath) t);
                }
                this.task.getLog().error(t);
            }
        }
    }

    public Log getLog() {
        return this.task.getLog();
    }

    public abstract void internalRun();
}

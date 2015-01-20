package uk.ac.open.kmi.forge.webPacketTracer.gateway;

import com.cisco.pt.ipc.IPCError;
import org.apache.commons.logging.Log;

public abstract class PTRunnable implements Runnable {

    final protected PTCommon task = new PTCommon();

    @Override
    public void run() {
        try {
            this.task.before();
            internalRun();
            this.task.after();
        } catch (IPCError ipcError) {
            this.task.getLog().error("\n\n\nAn IPC error occurred:\n\t" + ipcError.getMessage() + "\n\n\n");
        } catch (Throwable t) {
            if ((t instanceof ThreadDeath)) {
                throw ((ThreadDeath) t);
            }
            this.task.getLog().error(t);
        } finally {
            try {
                if (this.task.packetTracerSession != null) {
                    this.task.packetTracerSession.close();
                }
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

package uk.ac.open.kmi.forge.webPacketTracer.gateway;

import com.cisco.pt.ipc.IPCError;

import java.util.concurrent.Callable;
import org.apache.commons.logging.Log;

public abstract class PTCallable<V> implements Callable<V> {

    final protected PTCommon task = new PTCommon();

    @Override
    public V call() {
        V ret = null;
        try {
            this.task.before();
            ret = internalRun();
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
            return ret;
        }
    }

    public Log getLog() {
        return this.task.getLog();
    }

    public abstract V internalRun();
}

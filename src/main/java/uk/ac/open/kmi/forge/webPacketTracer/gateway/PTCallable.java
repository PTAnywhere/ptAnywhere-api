package uk.ac.open.kmi.forge.webPacketTracer.gateway;

import com.cisco.pt.ipc.IPCError;

import java.util.concurrent.Callable;
import org.apache.commons.logging.Log;

public abstract class PTCallable<V> implements Callable<V> {

    final protected PTConnection connection;

    public PTCallable() {
        this.connection = PTConnection.createPacketTracerGateway();
    }

    @Override
    public V call() {
        V ret = null;
        try {
            this.connection.before();
            ret = internalRun();
        } catch (IPCError ipcError) {
            this.connection.getLog().error("\n\n\nAn IPC error occurred:\n\t" + ipcError.getMessage() + "\n\n\n");
        } catch (Throwable t) {
            if ((t instanceof ThreadDeath)) {
                throw ((ThreadDeath) t);
            }
            this.connection.getLog().error(t);
        } finally {
            this.connection.close();
            return ret;
        }
    }

    public Log getLog() {
        return this.connection.getLog();
    }

    public abstract V internalRun();
}

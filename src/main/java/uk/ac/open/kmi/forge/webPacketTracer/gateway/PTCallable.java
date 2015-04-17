package uk.ac.open.kmi.forge.webPacketTracer.gateway;

import com.cisco.pt.ipc.IPCError;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.webPacketTracer.api.http.SessionNotFoundException;
import uk.ac.open.kmi.forge.webPacketTracer.session.PTInstanceDetails;
import uk.ac.open.kmi.forge.webPacketTracer.session.SessionManager;

public abstract class PTCallable<V> implements Callable<V> {

    private static final Log LOGGER = LogFactory.getLog(PTCallable.class);

    final SessionManager sm;

    /**
     * This object can be used inside internalRun method, but not outside as it might not be initialized.
     */
    protected PTConnection connection;

    public PTCallable(SessionManager sm) {
        this.sm = sm;
    }

    @Override
    public V call() {
        final PTInstanceDetails details = this.sm.getInstance();
        this.connection = PTConnection.createPacketTracerGateway(details.getHost(), details.getPort());
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
            LOGGER.error(t);
        } finally {
            this.connection.close();
            return ret;
        }
    }

    public abstract V internalRun();
}

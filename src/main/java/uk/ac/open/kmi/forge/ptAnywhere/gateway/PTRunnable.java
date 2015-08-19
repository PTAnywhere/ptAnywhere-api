package uk.ac.open.kmi.forge.ptAnywhere.gateway;

import com.cisco.pt.ipc.IPCError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.exceptions.PacketTracerConnectionException;
import uk.ac.open.kmi.forge.ptAnywhere.session.PTInstanceDetails;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionManager;


public abstract class PTRunnable implements Runnable {

    private static final Log LOGGER = LogFactory.getLog(PTRunnable.class);

    final SessionManager sm;

    /**
     * This object can be used inside internalRun method, but not outside as it might not be initialized.
     */
    protected PTConnection connection;

    public PTRunnable(SessionManager sm) {
        this.sm = sm;
    }

   @Override
    public void run() {
        final PTInstanceDetails details = this.sm.getInstance();
        this.connection = PTConnection.createPacketTracerGateway(details.getHost(), details.getPort());
        try {
            this.connection.before();
            internalRun();
        } catch (IPCError ipcError) {
            this.connection.getLog().error("\n\n\nAn IPC error occurred:\n\t" + ipcError.getMessage() + "\n\n\n");
            throw new PacketTracerConnectionException(ipcError.getMessage(), ipcError);
        } catch (PacketTracerConnectionException ptce) {
            throw ptce;
        } catch (Throwable t) {
            // More general errors...
            if (t instanceof ThreadDeath) {
                throw ((ThreadDeath) t);
            }
            // Awful errors in the library.
            // Simply log them (maybe in the future I'll be able to contribute to that library) and then move on.
            LOGGER.error(t.getMessage(), t);
            throw new PacketTracerConnectionException();
        } finally {
            this.connection.close();
        }
    }

    public abstract void internalRun();
}

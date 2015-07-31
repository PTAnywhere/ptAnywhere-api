package uk.ac.open.kmi.forge.ptAnywhere.gateway;

import com.cisco.pt.ipc.IPCError;
import com.cisco.pt.ipc.IPCFactory;
import com.cisco.pt.ipc.events.TerminalLineEventRegistry;
import com.cisco.pt.ipc.ui.IPC;
import com.cisco.pt.ptmp.ConnectionNegotiationProperties;
import com.cisco.pt.ptmp.PacketTracerSession;
import com.cisco.pt.ptmp.PacketTracerSessionFactory;
import com.cisco.pt.ptmp.impl.PacketTracerSessionFactoryImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.exceptions.PacketTracerConnectionException;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PacketTracerInstanceProperties;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PropertyFileManager;

import java.io.IOException;
import java.util.Iterator;


/**
 * Class which communicated with PacketTracer but needs to be manually started and stoped (possibly by different threads).
 */
public class PTConnection {

    final protected String hostName;
    final protected int port;

    private static final Log LOGGER = LogFactory.getLog(PTConnection.class);

    protected PacketTracerSession packetTracerSession;
    protected IPCFactory ipcFactory;

    protected PTConnection(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public static PTConnection createDefaultPacketTracerGateway() {
        final PropertyFileManager pfm = new PropertyFileManager();
        final Iterator<PacketTracerInstanceProperties> it = pfm.getPacketTracerInstancesDetails().iterator();
        if(!it.hasNext()) {
            LOGGER.error("PT instances could not be read from the properties file.");
            throw new RuntimeException("Backend instance not found.");
        }
        final PacketTracerInstanceProperties prop = it.next();
        return new PTConnection(prop.getHostname(), prop.getPort());
    }

    public static PTConnection createPacketTracerGateway(String hostName, int port) {
        return new PTConnection(hostName, port);
    }

    public Log getLog() {
        return LOGGER;
    }

    protected void before() throws IOException {
        final PacketTracerSessionFactory sessionFactory = PacketTracerSessionFactoryImpl.getInstance();
        this.packetTracerSession = createSession(sessionFactory);
        this.ipcFactory = new IPCFactory(this.packetTracerSession);
    }

    protected void after() throws IOException {
        if (this.packetTracerSession != null) {
            this.packetTracerSession.close();
        }
    }

    protected PacketTracerSession createSession(PacketTracerSessionFactory sessionFactory)
            throws IOException, PacketTracerConnectionException {
        final ConnectionNegotiationProperties negotiationProperties = getNegotiationProperties();
        try {
            if (negotiationProperties == null) {
                return createDefaultSession(sessionFactory);
            }
            return createSession(sessionFactory, negotiationProperties);
        } catch(Error e) {
            if (e.getMessage().equals("Unable to connect to Packet Tracer"))
                throw new PacketTracerConnectionException();
            throw e;  // else
        }
    }

    protected PacketTracerSession createDefaultSession(PacketTracerSessionFactory sessionFactory)
            throws IOException {
        return sessionFactory.openSession(this.hostName, this.port);
    }

    protected PacketTracerSession createSession(PacketTracerSessionFactory sessionFactory, ConnectionNegotiationProperties negotiationProperties)
            throws IOException, PacketTracerConnectionException {
        return sessionFactory.openSession(this.hostName, this.port, negotiationProperties);
    }

    protected ConnectionNegotiationProperties getNegotiationProperties() {
        return null;
    }

    public IPC getIPC() {
        return this.ipcFactory.getIPC();
    }

    public PacketTracerDAO getDataAccessObject() {
        return new PacketTracerDAO(getIPC());
    }

    public void open() {
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

    public void close() {
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

    public TerminalLineEventRegistry getTerminalLineEventRegistry() {
        return this.packetTracerSession.getEventManager().getTerminalLineEvents();
    }
}

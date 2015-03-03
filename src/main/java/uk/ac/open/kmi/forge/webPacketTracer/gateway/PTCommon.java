package uk.ac.open.kmi.forge.webPacketTracer.gateway;

import com.cisco.pt.ipc.IPCFactory;
import com.cisco.pt.ipc.ui.IPC;
import com.cisco.pt.ptmp.ConnectionNegotiationProperties;
import com.cisco.pt.ptmp.PacketTracerSession;
import com.cisco.pt.ptmp.PacketTracerSessionFactory;
import com.cisco.pt.ptmp.impl.PacketTracerSessionFactoryImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;


public abstract class PTCommon {
    protected PacketTracerSession packetTracerSession;
    protected IPCFactory ipcFactory;
    final protected String hostName;
    final protected int port;
    private static final Log LOGGER = LogFactory.getLog(PTCommon.class);

    public PTCommon() {
        //this("KMI-APPSVR03", 40000);
        this("localhost", 39000);
    }

    public PTCommon(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public Log getLog() {
        return LOGGER;
    }

    protected void before() throws IOException {
        PacketTracerSessionFactory sessionFactory = PacketTracerSessionFactoryImpl.getInstance();
        this.packetTracerSession = createSession(sessionFactory);
        this.ipcFactory = new IPCFactory(this.packetTracerSession);
    }

    protected void after() throws IOException {
        if (this.packetTracerSession != null) {
            this.packetTracerSession.close();
        }
    }

    protected PacketTracerSession createSession(PacketTracerSessionFactory sessionFactory)
            throws IOException {
        final ConnectionNegotiationProperties negotiationProperties = getNegotiationProperties();
        if (negotiationProperties == null) {
            return createDefaultSession(sessionFactory);
        }
        return createSession(sessionFactory, negotiationProperties);
    }

    protected PacketTracerSession createDefaultSession(PacketTracerSessionFactory sessionFactory)
            throws IOException {
        return sessionFactory.openSession(this.hostName, this.port);
    }

    protected PacketTracerSession createSession(PacketTracerSessionFactory sessionFactory, ConnectionNegotiationProperties negotiationProperties)
            throws IOException {
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
}

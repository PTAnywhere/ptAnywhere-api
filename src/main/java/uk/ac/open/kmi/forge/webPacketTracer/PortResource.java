package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.IPAddress;
import com.cisco.pt.impl.IPAddressImpl;
import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.sim.port.HostPort;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


abstract class AbstractPortHandler extends PTCallable<Port> {

    final String deviceId;
    String portName;
    com.cisco.pt.ipc.sim.Device device;  // Used in link resource!
    com.cisco.pt.ipc.sim.port.Port cachedPort = null;

    public AbstractPortHandler(String deviceId, String portNamee) {
        this.deviceId = deviceId;
        // FIXME: Issue with names containing slashes or backslashes and tomcat6.
        // http://stackoverflow.com/questions/2291428/jax-rs-pathparam-how-to-pass-a-string-with-slashes-hyphens-equals-too
        // To overcome it, I replaced slashes with spaces...
        try {
            portNamee = URLDecoder.decode(portNamee, "UTF-8");
            this.portName = portNamee.replace(" ", "/");
        } catch (UnsupportedEncodingException e) {
            getLog().error("Apparently UTF-8 does not exist as an encoding :-S", e);
            this.portName = portNamee; // This should never happen!
        }
    }

    private void loadDevice() {
        final Network network = this.task.getIPC().network();
        for (int i = 0; i < network.getDeviceCount(); i++) {
            final com.cisco.pt.ipc.sim.Device ret = network.getDeviceAt(i);
            if (this.deviceId.equals(ret.getObjectUUID().getDecoratedHexString())) {
                this.device = ret;
                break;
            }
        }
    }

    private void loadPort() {
        loadDevice();
        if (this.device==null) return;
        for(int i=0; i<this.device.getPortCount(); i++) {
            final com.cisco.pt.ipc.sim.port.Port port = this.device.getPortAt(i);
            if (this.portName.equals(port.getName())) {
                this.cachedPort = port;
                break;
            }
        }
    }

    public com.cisco.pt.ipc.sim.port.Port getPTPort() {
        if (this.cachedPort==null) loadPort();
        return this.cachedPort;
    }

    public Port getPort() {
        return Port.fromCiscoObject(getPTPort());
    }

    public Device getDevice() {
        if (this.device==null) loadDevice();
        return Device.fromCiscoObject(this.device);
    }
}

class PortGetter extends AbstractPortHandler {
    public PortGetter(String deviceId, String portName) {
        super(deviceId, portName);
    }
    @Override
    public Port internalRun() {
        return getPort();
    }
}

class PortModifier extends AbstractPortHandler {

    final Port modification;

    public PortModifier(String deviceId, String portName, Port modification) {
        super(deviceId, portName);
        this.modification = modification;
    }

    @Override
    public Port internalRun() {
        final com.cisco.pt.ipc.sim.port.Port p = getPTPort();
        if (p!=null) {
            if (p instanceof HostPort) {
                final IPAddress ip = new IPAddressImpl(this.modification.getPortIpAddress());
                final IPAddress subnet = new IPAddressImpl(this.modification.getPortSubnetMask());
                ((HostPort) p).setIpSubnetMask(ip, subnet);
            }
            return getPort();
        }
        return null;
    }
}

@Path("devices/{device}/ports/{port}")
public class PortResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Port getPort(
            @PathParam("device") String deviceId,
            @PathParam("port") String portName) {
        return new PortGetter(deviceId, portName).call();  // Not using a new Thread
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Port modifyPort(
            Port modification,
            @PathParam("device") String deviceId,
            @PathParam("port") String portName) {
        return new PortModifier(deviceId, portName, modification).call();  // Not using a new Thread
    }
}

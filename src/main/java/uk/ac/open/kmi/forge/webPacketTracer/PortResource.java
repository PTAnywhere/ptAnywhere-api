package uk.ac.open.kmi.forge.webPacketTracer;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;

class PortGetter extends PTCallable<Port> {
    final String deviceId;
    String portName;
    public PortGetter(String deviceId, String portNamee) {
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
    @Override
    public Port internalRun() {
        final Collection<Port> ports = new DeviceGetterById(this.deviceId).call().getPorts(); // FIXME Not optimal (quick and dirty solution)
        for(Port port: ports) {
            if (this.portName.equals(port.getPortName())) {
                return port;
            }
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
}

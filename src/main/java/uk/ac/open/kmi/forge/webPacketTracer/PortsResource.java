package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.ipc.sim.Network;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

class PortsGetter extends PTCallable<Collection<Port>> {
    final String deviceName;
    public PortsGetter(String deviceName) {
        this.deviceName = deviceName;
    }
    @Override
    public Collection<Port> internalRun() {
        return new DeviceGetterById(this.deviceName).call().getPorts();
    }
}

@Path("devices/{device}/ports")
public class PortsResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Port> getDevice(
            @PathParam("device") String deviceId) {
        return new PortsGetter(deviceId).call();  // Not using a new Thread
    }
}

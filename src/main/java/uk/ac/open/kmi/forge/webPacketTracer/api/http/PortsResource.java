package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

class PortsGetter extends PTCallable<Collection<Port>> {
    final String deviceId;
    final boolean byName;
    public PortsGetter(String deviceId, boolean isName) {
        this.deviceId = deviceId;
        this.byName = isName;
    }
    @Override
    public Collection<Port> internalRun() {
        return this.connection.getDataAccessObject().getPorts(this.deviceId, this.byName);
    }
}

@Path("devices/{device}/ports")
public class PortsResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Port> getPorts(
            @PathParam("device") String deviceId,
            @DefaultValue("false") @QueryParam("byName") boolean byName) {
        return new PortsGetter(deviceId, byName).call();  // Not using a new Thread
    }
}

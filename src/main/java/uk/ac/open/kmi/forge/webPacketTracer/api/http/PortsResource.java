package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Collection;
import java.util.Iterator;

class PortsGetter extends PTCallable<Collection<Port>> {
    final String deviceId;
    final boolean byName;
    public PortsGetter(String deviceId, boolean byName) {
        this.deviceId = deviceId;
        this.byName = byName;
    }
    @Override
    public Collection<Port> internalRun() {
        return this.connection.getDataAccessObject().getPorts(this.deviceId, this.byName);
    }
}

@Path("devices/{device}/ports")
public class PortsResource {
    @Context UriInfo uri;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPorts(
            @PathParam("device") String deviceId,
            @DefaultValue("false") @QueryParam("byName") boolean byName) {
        final Collection<Port> p = new PortsGetter(deviceId, byName).call();  // Not using a new Thread
        // To array because otherwise Response does not know how to serialize Collection<Device>
        return Response.ok(p.toArray(new Port[p.size()])).
                links(getDeviceLink(deviceId)).
                links(getPortLinks(p)).build();
    }

    private Link getDeviceLink(String deviceId) {
        return Link.fromUri(this.uri.getBaseUri() + "devices/" + Utils.encodeForURL(deviceId)).rel("device").build();
    }

    private Link getPortLink(String portName) {
        return Link.fromUri(this.uri.getRequestUri() + "/" + Utils.escapePort(portName)).rel("item").build();
    }

    private Link[] getPortLinks(Collection<Port> ports) {
        final Link[] links = new Link[ports.size()];
        final Iterator<Port> portIt = ports.iterator();
        for(int i=0; i<links.length; i++) {
            links[i] = getPortLink(portIt.next().getPortName());
        }
        return links;
    }
}

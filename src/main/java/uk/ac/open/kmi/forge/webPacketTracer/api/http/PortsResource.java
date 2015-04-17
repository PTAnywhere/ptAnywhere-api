package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;
import uk.ac.open.kmi.forge.webPacketTracer.session.SessionManager;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Collection;
import java.util.Iterator;

class PortsGetter extends PTCallable<Collection<Port>> {
    final String deviceId;
    final boolean byName;
    final boolean filterFree;
    public PortsGetter(SessionManager sm, String deviceId, boolean byName, boolean filterFree) {
        super(sm);
        this.deviceId = deviceId;
        this.byName = byName;
        this.filterFree = filterFree;
    }
    @Override
    public Collection<Port> internalRun() {
        return this.connection.getDataAccessObject().getPorts(this.deviceId, this.byName, this.filterFree);
    }
}

public class PortsResource {

    final UriInfo uri;
    final SessionManager sm;
    public PortsResource(UriInfo uri, SessionManager sm) {
        this.uri = uri;
        this.sm = sm;
    }

    @Path("{" + PortResource.PORT_PARAM + "}")
    public PortResource getResource(@Context UriInfo u) {
        return new PortResource(u, this.sm);
    }

    // TODO return 'self' links (at least when byName==true)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPorts(
            @PathParam(DeviceResource.DEVICE_PARAM) String deviceId,
            @DefaultValue("false") @QueryParam("byName") boolean byName,
            @DefaultValue("false") @QueryParam("free") boolean filterFree) {
        final Collection<Port> p = new PortsGetter(this.sm, deviceId, byName, filterFree).call();  // Not using a new Thread
        // To array because otherwise Response does not know how to serialize Collection<Device>
        return Response.ok(p.toArray(new Port[p.size()])).
                links(getDeviceLink(deviceId)).
                links(getPortLinks(p)).build();
    }

    private Link getDeviceLink(String deviceId) {
        return Link.fromUri(this.uri.getBaseUri() + "devices/" + Utils.encodeForURL(deviceId)).rel("device").build();
    }

    private Link getPortLink(String portName) {
        return Link.fromUri(Utils.getURIWithSlashRemovingQuery(this.uri.getRequestUri()) + Utils.escapePort(portName)).rel("item").build();
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

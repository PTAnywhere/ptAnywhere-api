package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import io.swagger.annotations.*;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.DeviceNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.ErrorBean;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.PacketTracerConnectionException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.SessionNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.gateway.PTCallable;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.Port;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionManager;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.PORT_PARAM;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.DEVICE_PARAM;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;

class PortsGetter extends PTCallable<Collection<Port>> {
    final String deviceId;
    final boolean byName;
    final boolean filterFree;
    final URLFactory uf;

    public PortsGetter(SessionManager sm, String deviceId, URI baseURI, boolean byName, boolean filterFree) {
        super(sm);
        this.deviceId = deviceId;
        this.byName = byName;
        this.filterFree = filterFree;
        this.uf = new URLFactory(baseURI, sm.getSessionId(), deviceId);
    }
    @Override
    public Collection<Port> internalRun() {
        final Collection<Port> ret = this.connection.getDataAccessObject().getPorts(this.deviceId, this.byName, this.filterFree);
        for(Port p: ret) {
            p.setURLFactory(this.uf);
        }
        return ret;
    }
}


@Api(hidden = true)
public class PortsResource {

    final UriInfo uri;
    final SessionManager sm;
    public PortsResource(UriInfo uri, SessionManager sm) {
        this.uri = uri;
        this.sm = sm;
    }

    @Path("{" + PORT_PARAM + "}")
    public PortResource getResource(@Context UriInfo u) {
        return new PortResource(u, this.sm);
    }

    // TODO return 'self' links (at least when byName==true)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieves all the ports of a device", response = Port.class, responseContainer = "set",
                    tags = "device")
    @ApiResponses(value = {
        @ApiResponse(code = DeviceNotFoundException.status, response = ErrorBean.class, message = DeviceNotFoundException.description),
        @ApiResponse(code = PacketTracerConnectionException.status, response = ErrorBean.class, message = PacketTracerConnectionException.description),
        @ApiResponse(code = SessionNotFoundException.status, response = ErrorBean.class, message = SessionNotFoundException.description)
    })
    public Response getPorts(
            @ApiParam(value = "Name or identifier of the device.") @PathParam(DEVICE_PARAM) String deviceId,
            @ApiParam(value = "Is the 'device' parameter the device name? (otherwise, it will be handled as its identifier)")
                @DefaultValue("false") @QueryParam("byName") boolean byName,
            @ApiParam(value = "Is the port available (i.e., not connected to another port)?")
                @DefaultValue("false") @QueryParam("free") boolean filterFree) {
        final Collection<Port> p = new PortsGetter(this.sm, deviceId, this.uri.getBaseUri(), byName, filterFree).call();
        // TODO add links to not found exception
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

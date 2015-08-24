package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import io.swagger.annotations.*;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.*;
import uk.ac.open.kmi.forge.ptAnywhere.gateway.PTCallable;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.Port;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionManager;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.DEVICE_PARAM;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.PORT_PARAM;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.PORT_LINK_PATH;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;


abstract class AbstractPortHandler extends PTCallable<Port> {
    final String deviceId;
    final URLFactory uf;
    public AbstractPortHandler(SessionManager sm, String deviceId, URI baseURI) {
        super(sm);
        this.deviceId = deviceId;
        this.uf = new URLFactory(baseURI, sm.getSessionId(), deviceId);
    }
    public Port internalRun() {
        final Port ret = handlePort();
        if (ret!=null) ret.setURLFactory(this.uf);
        return ret;
    }
    public abstract Port handlePort();
}

class PortGetter extends AbstractPortHandler {
    final String portName;
    public PortGetter(SessionManager sm, String deviceId, String portName, URI baseURI) {
        super(sm, deviceId, baseURI);
        this.portName = portName;
    }
    @Override
    public Port handlePort() {
        return this.connection.getDataAccessObject().getPort(this.deviceId, this.portName);
    }
}

class PortModifier extends AbstractPortHandler {
    final Port modification;
    public PortModifier(SessionManager sm, String deviceId, Port modification, URI baseURI) {
        super(sm, deviceId, baseURI);
        this.modification = modification;
    }

    @Override
    public Port handlePort() {
        return this.connection.getDataAccessObject().modifyPort(this.deviceId, this.modification);
    }
}


@Api(hidden = true)
public class PortResource {

    final UriInfo uri;
    final SessionManager sm;
    public PortResource(UriInfo uri, SessionManager sm) {
        this.uri = uri;
        this.sm = sm;
    }

    @Path(PORT_LINK_PATH)
    public PortLinkResource getResource(@Context UriInfo u) {
        return new PortLinkResource(u, this.sm);
    }

    // Consider byName==true (or at least put a redirection or self element)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieves the details of the port", response = Port.class, tags = "device")
    @ApiResponses(value = {
        @ApiResponse(code = PortNotFoundException.status, response = ErrorBean.class, message = PortNotFoundException.description),
        @ApiResponse(code = PacketTracerConnectionException.status, response = ErrorBean.class, message = PacketTracerConnectionException.description),
        @ApiResponse(code = SessionNotFoundException.status, response = ErrorBean.class, message = SessionNotFoundException.description)
    })
    public Response getPort(
            @ApiParam(value = "Identifier of the device.") @PathParam(DEVICE_PARAM) String deviceId,
            @ApiParam(value = "Name of the port inside the device.") @PathParam(PORT_PARAM) String portName) {
        final Port p = new PortGetter(this.sm, deviceId, Utils.unescapePort(portName), this.uri.getBaseUri()).call();  // Not using a new Thread
        // TODO add links to not found exception
        return Response.ok(p).
                links(getPortsLink()).
                // Link resource returned even if it does not exist because
                // the /link resource is still there and can be used to create a new one.
                links(getLinkLink()).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates the details of the port", response = Port.class, tags = "device")
    @ApiResponses(value = {
        @ApiResponse(code = PortNotFoundException.status, response = ErrorBean.class, message = PortNotFoundException.description),
        @ApiResponse(code = PacketTracerConnectionException.status, response = ErrorBean.class, message = PacketTracerConnectionException.description),
        @ApiResponse(code = SessionNotFoundException.status, response = ErrorBean.class, message = SessionNotFoundException.description)
    })
    public Response modifyPort(
            @ApiParam(value = "Port to be modified. Only the IP address and the subnet mask can be updated " +
                    "(providing this makes sense for this type of device). " +
                    "The rest of the fields will be ignored.") Port modification,
            @ApiParam(value = "Identifier of the device.") @PathParam(DEVICE_PARAM) String deviceId,
            @ApiParam(value = "Name of the port inside the device.") @PathParam(PORT_PARAM) String portName) {
        // The portName should be provided in the URL, not in the body (i.e., JSON sent).
        if (modification.getPortName()==null) {
            modification.setPortName(Utils.unescapePort(portName));
            final Port ret = new PortModifier(this.sm, deviceId, modification, this.uri.getBaseUri()).call();  // Not using a new Thread
            // TODO add links to not found exception
            return Response.ok(ret).
                    links(getPortsLink()).
                    links(getLinkLink()).build();
        } else
            // throw new BadRequestException(); //Returns HTML
            return Response.status(Response.Status.BAD_REQUEST).entity(modification).
                    links(getPortsLink()).build();  // /link not returned on error because this resource may not exist.
    }

    private Link getPortsLink() {
        return Link.fromUri(Utils.getParent(this.uri.getRequestUri())).rel("collection").build();
    }

    private Link getLinkLink() {
        // Should I rename it to "connection" to avoid misunderstandings?
        return Link.fromUri(Utils.getURIWithSlashRemovingQuery(this.uri.getRequestUri()) + "link").rel("link").build();
    }
}
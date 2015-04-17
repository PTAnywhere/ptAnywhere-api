package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;
import uk.ac.open.kmi.forge.webPacketTracer.session.SessionManager;

import javax.ws.rs.*;
import javax.ws.rs.core.*;


class PortGetter extends PTCallable<Port> {
    final String deviceId;
    final String portName;
    public PortGetter(SessionManager sm, String deviceId, String portName) {
        super(sm);
        this.deviceId = deviceId;
        this.portName = portName;
    }
    @Override
    public Port internalRun() {
        return this.connection.getDataAccessObject().getPort(this.deviceId, this.portName);
    }
}

class PortModifier extends PTCallable<Port> {
    final String deviceId;
    final Port modification;
    public PortModifier(SessionManager sm, String deviceId, Port modification) {
        super(sm);
        this.deviceId = deviceId;
        this.modification = modification;
    }

    @Override
    public Port internalRun() {
        return this.connection.getDataAccessObject().modifyPort(this.deviceId, this.modification);
    }
}

public class PortResource {

    static final public String PORT_PARAM = "port";

    final UriInfo uri;
    final SessionManager sm;
    public PortResource(UriInfo uri, SessionManager sm) {
        this.uri = uri;
        this.sm = sm;
    }

    @Path("link")
    public PortLinkResource getResource(@Context UriInfo u) {
        return new PortLinkResource(u, this.sm);
    }

    // Consider byName==true (or at least put a redirection or self element)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPort(
            @PathParam(DeviceResource.DEVICE_PARAM) String deviceId,
            @PathParam(PORT_PARAM) String portName) {
        final Port p = new PortGetter(this.sm, deviceId, Utils.unescapePort(portName)).call();  // Not using a new Thread
        if (p==null)
            return Response.noContent().
                    links(getPortsLink()).build();
        return Response.ok(p).
                links(getPortsLink()).
                // Link resource returned even if it does not exist because
                // the /link resource is still there and can be used to create a new one.
                links(getLinkLink()).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyPort(
            Port modification,
            @PathParam(DeviceResource.DEVICE_PARAM) String deviceId,
            @PathParam(PORT_PARAM) String portName) {
        // The portName should be provided in the URL, not in the body (i.e., JSON sent).
        if (modification.getPortName()==null) {
            modification.setPortName(Utils.unescapePort(portName));
            final Port ret = new PortModifier(this.sm, deviceId, modification).call();  // Not using a new Thread
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
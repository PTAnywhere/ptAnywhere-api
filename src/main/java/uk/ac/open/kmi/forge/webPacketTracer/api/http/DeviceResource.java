package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.analytics.InteractionRecord;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.session.SessionManager;
import static uk.ac.open.kmi.forge.webPacketTracer.api.http.URLFactory.DEVICE_PARAM;
import static uk.ac.open.kmi.forge.webPacketTracer.api.http.URLFactory.PORT_PATH;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;

abstract class AbstractDeviceHandler extends PTCallable<Device> {
    final String dId;
    final URLFactory uf;
    public AbstractDeviceHandler(SessionManager sm, String dId, URI baseURI) {
        super(sm);
        this.dId = dId;
        this.uf = new URLFactory(baseURI, sm.getSessionId());
    }
    @Override
    public Device internalRun() {
        final Device ret = manageDevice();
        if(ret!=null) ret.setURLFactory(this.uf);
        return ret;
    }

    abstract Device manageDevice();
}

class DeviceGetter extends AbstractDeviceHandler {
    final boolean byName;
    public DeviceGetter(SessionManager sm, String dId, boolean byName, URI baseURI) {
        super(sm, dId, baseURI);
        this.byName = byName;
    }
    @Override
    public Device manageDevice() {
        if (this.byName) {
            return this.connection.getDataAccessObject().getDeviceByName(this.dId);
        } else {
            return this.connection.getDataAccessObject().getDeviceById(this.dId);
        }
    }
}

class DeviceDeleter extends AbstractDeviceHandler {
    public DeviceDeleter(SessionManager sm, String dId, URI baseURI) {
        super(sm, dId, baseURI);
    }
    @Override
    public Device manageDevice() {
        return this.connection.getDataAccessObject().removeDevice(this.dId);
    }
}

class DeviceModifier extends AbstractDeviceHandler {
    final Device modification;
    public DeviceModifier(SessionManager sm, String dId, Device modification, URI baseURI) {
        super(sm, dId, baseURI);
        modification.setId(dId);
        this.modification = modification;
    }
    @Override
    public Device manageDevice() {
        return this.connection.getDataAccessObject().modifyDevice(this.modification);
    }
}

public class DeviceResource {

    final UriInfo uri;
    final SessionManager sm;

    public DeviceResource(UriInfo uri, SessionManager sm) {
        this.uri = uri;
        this.sm = sm;
    }

    @Path(PORT_PATH)
    public PortsResource getResource(@Context UriInfo u) {
        return new PortsResource(u, this.sm);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevice(
        @PathParam(DEVICE_PARAM) String deviceId,
        @DefaultValue("false") @QueryParam("byName") boolean byName) {
        final Device d = new DeviceGetter(this.sm, deviceId, byName, this.uri.getBaseUri()).call();  // Not using a new Thread
        if (d==null)
            return Response.noContent().
                    links(getDevicesLink()).build();
        return Response.ok(d).
                links(getDeviceLink(d, !byName)).  // If the device was accessed by name, return id-based URI (and viceversa).
                links(getDevicesLink()).
                links(getPortsLink(d)).build();
    }

    // FIXME DELETE and PUT should also consider the 'byName' parameter.
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeDevice(@PathParam(DEVICE_PARAM) String deviceId,
                                 @Context ServletContext servletContext) {
        final Device d = new DeviceDeleter(this.sm, deviceId, this.uri.getBaseUri()).call();  // Not using a new Thread
        if (d==null)
            return Response.noContent().
                    links(getDevicesLink()).build();

        final InteractionRecord ir = Utils.createInteractionRecord(servletContext);
        ir.deviceDeleted(this.sm.getSessionId(), this.uri.getRequestUri().toString(), d.getLabel(), d.getGroup());
        return Response.ok(d).
                links(getDevicesLink()).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyDevice(
            Device modification,
            @PathParam(DEVICE_PARAM) String deviceId,
            @Context ServletContext servletContext) {
        final Device d = new DeviceModifier(this.sm, deviceId, modification, this.uri.getBaseUri()).call();  // Not using a new Thread
        if (d==null)
            return Response.noContent().
                    links(getDevicesLink()).build();
        final InteractionRecord ir = Utils.createInteractionRecord(servletContext);
        ir.deviceModified(this.sm.getSessionId(), this.uri.getRequestUri().toString(), d.getLabel(), d.getGroup());
        return Response.ok(d).
                links(getDevicesLink()).
                links(getPortsLink(d)).build();
    }

    private Link getPortsLink(Device d) {
        return Link.fromUri(getSelfURIById(d) + "/" + PORT_PATH).rel("ports").build();
        // More clear but only valid when byName==false
        // return Link.fromUri(this.uri.getRequestUri() + "/ports").rel("ports").build();
    }

    private Link getDevicesLink() {
        return Link.fromUri(Utils.getParent(this.uri.getRequestUri())).rel("collection").build();
    }

    private String getSelfURIById(Device d) {
        return Utils.getParent(this.uri.getRequestUri()) + Utils.encodeForURL(d.getId());
    }

    private Link getDeviceLink(Device d, boolean byName) {
        if (byName)
            return Link.fromUri(Utils.getParent(this.uri.getRequestUri()) + d.getLabel() + "?byName=true").rel("self").build();
        return Link.fromUri(getSelfURIById(d)).rel("self").build();
    }
}
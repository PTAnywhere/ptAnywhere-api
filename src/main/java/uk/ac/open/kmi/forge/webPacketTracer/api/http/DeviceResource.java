package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.analytics.InteractionRecord;
import uk.ac.open.kmi.forge.webPacketTracer.analytics.InteractionRecordFactory;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.session.SessionManager;

import javax.ws.rs.*;
import javax.ws.rs.core.*;


class DeviceGetter extends PTCallable<Device> {
    final String dId;
    final boolean byName;
    public DeviceGetter(SessionManager sm, String dId, boolean byName) {
        super(sm);
        this.dId = dId;
        this.byName = byName;
    }
    @Override
    public Device internalRun() {
        if (this.byName) {
            return this.connection.getDataAccessObject().getDeviceByName(this.dId);
        } else {
            return this.connection.getDataAccessObject().getDeviceById(this.dId);
        }
    }
}

class DeviceDeleter extends PTCallable<Device> {
    final String dId;
    public DeviceDeleter(SessionManager sm, String dId) {
        super(sm);
        this.dId = dId;
    }
    @Override
    public Device internalRun() {
        return this.connection.getDataAccessObject().removeDevice(this.dId);
    }
}

class DeviceModifier extends PTCallable<Device> {
    final Device modification;
    public DeviceModifier(SessionManager sm, String dId, Device modification) {
        super(sm);
        modification.setId(dId);
        this.modification = modification;
    }
    @Override
    public Device internalRun() {
        return this.connection.getDataAccessObject().modifyDevice(this.modification);
    }
}

public class DeviceResource {
    static final public String DEVICE_PARAM = "device";
    final UriInfo uri;
    final SessionManager sm;
    public DeviceResource(UriInfo uri, SessionManager sm) {
        this.uri = uri;
        this.sm = sm;
    }

    @Path("ports")
    public PortsResource getResource(@Context UriInfo u) {
        return new PortsResource(u, this.sm);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevice(
        @PathParam(DEVICE_PARAM) String deviceId,
        @DefaultValue("false") @QueryParam("byName") boolean byName) {
        final Device d = new DeviceGetter(this.sm, deviceId, byName).call();  // Not using a new Thread
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
    public Response removeDevice(@PathParam("device") String deviceId) {
        final Device d = new DeviceDeleter(this.sm, deviceId).call();  // Not using a new Thread
        if (d==null)
            return Response.noContent().
                    links(getDevicesLink()).build();
        final InteractionRecord ir = InteractionRecordFactory.create();
        ir.deviceDeleted(sm.getSessionId(), this.uri.getRequestUri().toString(), d.getLabel(), d.getGroup());
        return Response.ok(d).
                links(getDevicesLink()).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyDevice(
            Device modification,
            @PathParam(DEVICE_PARAM) String deviceId) {
        final Device d = new DeviceModifier(this.sm, deviceId, modification).call();  // Not using a new Thread
        if (d==null)
            return Response.noContent().
                    links(getDevicesLink()).build();
        final InteractionRecord ir = InteractionRecordFactory.create();
        ir.deviceModified(sm.getSessionId(), this.uri.getRequestUri().toString(), d.getLabel(), d.getGroup());
        return Response.ok(d).
                links(getDevicesLink()).
                links(getPortsLink(d)).build();
    }

    private Link getPortsLink(Device d) {
        return Link.fromUri(getSelfURIById(d) + "/ports").rel("ports").build();
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
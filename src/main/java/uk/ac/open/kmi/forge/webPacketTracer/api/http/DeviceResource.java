package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

class DeviceGetter extends PTCallable<Device> {
    final String dId;
    final boolean byName;
    public DeviceGetter(String dId, boolean byName) {
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
    public DeviceDeleter(String dId) {
        this.dId = dId;
    }
    @Override
    public Device internalRun() {
        return this.connection.getDataAccessObject().removeDevice(this.dId);
    }
}

class DeviceModifier extends PTCallable<Device> {
    final Device modification;
    public DeviceModifier(String dId, Device modification) {
        modification.setId(dId);
        this.modification = modification;
    }
    @Override
    public Device internalRun() {
        return this.connection.getDataAccessObject().modifyDevice(this.modification);
    }
}

@Path("devices/{device}")
public class DeviceResource {
    @Context
    UriInfo uri;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevice(
        @PathParam("device") String deviceId,
        @DefaultValue("false") @QueryParam("byName") boolean byName) {
        final Device d = new DeviceGetter(deviceId, byName).call();  // Not using a new Thread
        if (d==null)
            return Response.noContent().
                    links(getDeviceLink()).build();
        return Response.ok(d).
                links(getDeviceLink()).
                links(getPortsLink()).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeDevice(@PathParam("device") String deviceId) {
        final Device d = new DeviceDeleter(deviceId).call();  // Not using a new Thread
        if (d==null)
            return Response.noContent().
                    links(getDeviceLink()).build();
        return Response.ok(d).
                links(getDeviceLink()).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyDevice(
            Device modification,
            @PathParam("device") String deviceId) {
        final Device d = new DeviceModifier(deviceId, modification).call();  // Not using a new Thread
        if (d==null)
            return Response.noContent().
                    links(getDeviceLink()).build();
        return Response.ok(d).
                links(getDeviceLink()).
                links(getPortsLink()).build();
    }

    private Link getPortsLink() {
        return Link.fromUri(this.uri.getRequestUri() + "/ports").rel("ports").build();
    }

    private Link getDeviceLink() {
        return Link.fromUri(this.uri.getRequestUri().resolve("..")).rel("collection").build();
    }
}
package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

class DeviceGetterByName extends PTCallable<Device> {
    final String name;
    public DeviceGetterByName(String name) {
        this.name = name;
    }
    @Override
    public Device internalRun() {
        return this.connection.getDataAccessObject().getDeviceByName(this.name);
    }
}

class DeviceGetterById extends PTCallable<Device> {
    final String dId;
    public DeviceGetterById(String dId) {
        this.dId = dId;
    }
    @Override
    public Device internalRun() {
        return this.connection.getDataAccessObject().getDeviceById(this.dId);
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
    final String dId;
    final Device modification;
    public DeviceModifier(String dId, Device modification) {
        this.dId = dId;
        this.modification = modification;
    }
    @Override
    public Device internalRun() {
        return this.connection.getDataAccessObject().modifyDevice(this.modification);
    }
}

@Path("devices/{device}")
public class DeviceResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Device getDevice(
        @PathParam("device") String deviceId,
        @DefaultValue("false") @QueryParam("byName") boolean byName) {
        if (byName) {
            return new DeviceGetterByName(deviceId).call();  // Not using a new Thread
        } else {
            return new DeviceGetterById(deviceId).call();  // Not using a new Thread
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Device removeDevice(@PathParam("device") String deviceId) {
        return new DeviceDeleter(deviceId).call();  // Not using a new Thread
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Device modifyDevice(
            Device modification,
            @PathParam("device") String deviceId) {
        return new DeviceModifier(deviceId, modification).call();  // Not using a new Thread
    }
}
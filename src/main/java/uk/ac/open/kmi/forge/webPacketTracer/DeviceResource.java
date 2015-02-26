package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.sim.port.HostPort;
import com.cisco.pt.ipc.ui.LogicalWorkspace;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PacketTracerDAO;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Set;

class DeviceGetterByName extends PTCallable<Device> {
    final String name;
    public DeviceGetterByName(String name) {
        this.name = name;
    }
    @Override
    public Device internalRun() {
        return this.task.getDataAccessObject().getDeviceByName(this.name);
    }
}

class DeviceGetterById extends PTCallable<Device> {
    final String dId;
    public DeviceGetterById(String dId) {
        this.dId = dId;
    }
    @Override
    public Device internalRun() {
        return this.task.getDataAccessObject().getDeviceById(this.dId);
    }
}

class DeviceDeleter extends PTCallable<Device> {
    final String dId;
    public DeviceDeleter(String dId) {
        this.dId = dId;
    }
    @Override
    public Device internalRun() {
        return this.task.getDataAccessObject().removeDevice(this.dId);
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
        return this.task.getDataAccessObject().modifyDevice(this.modification);
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
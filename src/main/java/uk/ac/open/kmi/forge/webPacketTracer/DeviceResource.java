package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.sim.port.HostPort;
import com.cisco.pt.ipc.ui.LogicalWorkspace;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Set;


abstract class AbstractDeviceHandler extends PTCallable<Device> {
    protected com.cisco.pt.ipc.sim.Device getDeviceByName(String deviceName) {
        final Network network = this.task.getIPC().network();
        return network.getDevice(deviceName);
    }

    protected com.cisco.pt.ipc.sim.Device getDeviceById(String deviceId) {
        final Network network = this.task.getIPC().network();
        for (int i = 0; i < network.getDeviceCount(); i++) {
            final com.cisco.pt.ipc.sim.Device ret = network.getDeviceAt(i);
            if (deviceId.equals(ret.getObjectUUID().getDecoratedHexString())) {
                return ret;
            }
        }
        return null;
    }

    protected Device toPOJODevice(com.cisco.pt.ipc.sim.Device d) {
        return Device.fromCiscoObject(d);
    }
}

class DeviceGetterByName extends AbstractDeviceHandler {
    final String name;
    public DeviceGetterByName(String name) {
        this.name = name;
    }
    @Override
    public Device internalRun() {
        return toPOJODevice(getDeviceByName(this.name));
    }
}

class DeviceGetterById extends AbstractDeviceHandler {
    final String dId;
    public DeviceGetterById(String dId) {
        this.dId = dId;
    }
    @Override
    public Device internalRun() {
        return toPOJODevice(getDeviceById(this.dId));
    }
}

class DeviceDeleter extends AbstractDeviceHandler {
    final String dId;
    public DeviceDeleter(String dId) {
        this.dId = dId;
    }
    @Override
    public Device internalRun() {
        final LogicalWorkspace workspace = this.task.getIPC().appWindow().getActiveWorkspace().getLogicalWorkspace();
        final com.cisco.pt.ipc.sim.Device d = getDeviceById(this.dId);
        workspace.removeDevice(d.getName());  // It can only be removed by name :-S
        return toPOJODevice(d);
    }
}

class DeviceModifier extends AbstractDeviceHandler {
    final String dId;
    final Device modification;
    public DeviceModifier(String dId, Device modification) {
        this.dId = dId;
        this.modification = modification;
    }
    @Override
    public Device internalRun() {
        final LogicalWorkspace workspace = this.task.getIPC().appWindow().getActiveWorkspace().getLogicalWorkspace();
        final com.cisco.pt.ipc.sim.Device d = getDeviceById(this.dId);
        d.setName(this.modification.getLabel());  // Right now, we only allow to change the name of the label!
        return toPOJODevice(d);
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
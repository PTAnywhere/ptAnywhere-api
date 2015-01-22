package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.sim.port.Link;
import com.cisco.pt.ipc.enums.DeviceType;
import com.cisco.pt.ipc.ui.LogicalWorkspace;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTRunnable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Edge;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;


class DevicesGetter extends PTCallable<Collection<Device>> {
    @Override
    public Collection<Device> internalRun() {
        final Network network = this.task.getIPC().network();
        final Set<Device> ret = new HashSet<Device>();
        for (int i = 0; i < network.getDeviceCount(); i++) {
            ret.add(Device.fromCiscoObject(network.getDeviceAt(i)));
        }
        return ret;
    }
}

class DevicePoster extends PTCallable<Device> {
    final Device d;
    final DeviceType type;
    final String name;
    public DevicePoster(Device d) {
        this.d = d;
        this.name = this.d.getLabel();
        final String g = this.d.getGroup();
        if (g.contains("switch")) {
            this.type = DeviceType.SWITCH;
        } else if (g.contains("router")) {
            this.type = DeviceType.ROUTER;
        } else if (g.contains("pc")) {
            this.type = DeviceType.PC;
        } else if (g.contains("cloud")) {
            this.type = DeviceType.CLOUD;
        } else {
            this.type = null;
        }
    }
    private String getDefaultModelName() {
        switch(this.type) {
            case SWITCH: return "2960-24TT";
            case ROUTER: return "2901";
            case PC: return "PC-PT";
            case CLOUD: return "Cloud-PT";
            default: return null;
        }
    }
    @Override
    public Device internalRun() {
        if(this.type==null) {
            getLog().error("Device type " + this.d.getGroup() + "not found.");
            // FIXME throw a more appropriate HTTP ERROR
            return null;  // This causes a HTTP 404
        } else {
            final LogicalWorkspace workspace = this.task.getIPC().appWindow().getActiveWorkspace().getLogicalWorkspace();
            final String addedDeviceName = workspace.addDevice(this.type, this.getDefaultModelName());
            final Network network = this.task.getIPC().network();
            final com.cisco.pt.ipc.sim.Device deviceAdded = network.getDevice(addedDeviceName);
            final Device ret = Device.fromCiscoObject(deviceAdded);
            // setName() somehow makes deviceAdded.getObjectUUID() return null.
            // That's why we set it at the end and without calling to getObjectUUID() afterwards (fromCiscoObject calls it).
            deviceAdded.setName(this.name);
            ret.setLabel(this.name);
            return ret;
        }
    }
}

@Path("devices")
public class DevicesResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Device createDevice(Device newDevice) {
        final DevicePoster poster = new DevicePoster(newDevice);
        return poster.call();  // Not using a new Thread
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Device> getJson() {
        final DevicesGetter getter = new DevicesGetter();
        return getter.call();  // Not using a new Thread
    }
}

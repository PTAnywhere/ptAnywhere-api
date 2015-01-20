package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.ipc.sim.Network;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Set;


class DeviceGetter extends PTCallable<Device> {

    final String deviceId;

    public DeviceGetter(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public Device internalRun() {
        final Network network = this.task.getIPC().network();
        final com.cisco.pt.ipc.sim.Device cDev = network.getDevice(this.deviceId);
        final Device d = Device.fromCiscoObject(cDev);
        return d;
    }
}

@Path("devices/{device}")
public class DeviceResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Device getDevice(@PathParam("device") String deviceName) {
        final DeviceGetter getter = new DeviceGetter(deviceName);
        return getter.call();  // Not using a new Thread
    }
}
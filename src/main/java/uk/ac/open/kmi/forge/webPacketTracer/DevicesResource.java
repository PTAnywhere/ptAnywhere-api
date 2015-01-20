package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.sim.port.Link;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Edge;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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


@Path("devices")
public class DevicesResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Device> getJson() {
        final DevicesGetter getter = new DevicesGetter();
        return getter.call();  // Not using a new Thread
    }
}

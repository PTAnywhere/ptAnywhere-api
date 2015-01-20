package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.sim.port.Link;
import com.cisco.pt.ipc.sim.port.Port;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Edge;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


class EdgesGetter extends PTCallable<Collection<Edge>> {

    @Override
    public Collection<Edge> internalRun() {
        final Network network = this.task.getIPC().network();
        final Map<String, Edge> ret = new HashMap<String, Edge>();
        for (int i = 0; i < network.getDeviceCount(); i++) {
            com.cisco.pt.ipc.sim.Device d = network.getDeviceAt(i);
            for (int j = 0; j < d.getPortCount(); j++) {
                final Port port = d.getPortAt(j);
                final Link currentLink = port.getLink();
                if (currentLink != null) {
                    final String linkId = currentLink.getObjectUUID().getDecoratedHexString();
                    final String devId = d.getObjectUUID().getDecoratedHexString();
                    if (ret.containsKey(linkId)) {
                        ret.get(linkId).setTo(devId);
                    } else {
                        ret.put(linkId, new Edge(linkId, devId, null));
                    }
                }
            }
        }
        return ret.values();
    }
}

@Path("edges")
public class EdgesResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Edge> getEdges() {
        final EdgesGetter getter = new EdgesGetter();
        return getter.call();  // Not using a new Thread
    }
}
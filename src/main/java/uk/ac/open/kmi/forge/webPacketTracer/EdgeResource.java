package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.sim.port.Link;
import com.cisco.pt.ipc.sim.port.Port;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Edge;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


class EdgeGetter extends PTCallable<Edge> {

    final String edgeId;

    public EdgeGetter(String edgeId) {
        this.edgeId = edgeId;
    }

    @Override
    public Edge internalRun() {
        final Network network = this.task.getIPC().network();
        Edge ret = null;
        for (int i = 0; i < network.getDeviceCount(); i++) {
            com.cisco.pt.ipc.sim.Device d = network.getDeviceAt(i);
            for (int j = 0; j < d.getPortCount(); j++) {
                final Port port = d.getPortAt(j);
                final Link currentLink = port.getLink();
                if (currentLink != null) {
                    final String linkId = currentLink.getObjectUUID().getDecoratedHexString();
                    if (this.edgeId.equals(linkId)) {
                        final String devId = d.getObjectUUID().getDecoratedHexString();
                        if (ret == null) {
                            ret = new Edge(this.edgeId, devId, null);
                        } else {
                            ret.setTo(devId);
                            break;
                        }
                    }
                }
            }
            if (ret != null && ret.getTo() != null) {
                break;
            }
        }
        return ret;
    }
}

@Path("edges/{edge}")
public class EdgeResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Edge getEdge(@PathParam("edge") String edgeId) {
        final EdgeGetter getter = new EdgeGetter(edgeId);
        return getter.call();  // Not using a new Thread
    }
}
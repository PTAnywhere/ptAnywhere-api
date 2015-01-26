package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.ipc.sim.port.Link;
import com.cisco.pt.ipc.sim.port.Port;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Edge;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Network;

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
        final com.cisco.pt.ipc.sim.Network network = this.task.getIPC().network();
        // Even if fromCiscoObject() returns more data than the one needed here,
        // it does not go over additional loops.
        return Network.fromCiscoObject(network).getEdges();
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
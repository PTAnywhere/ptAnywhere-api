package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Network;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

class AllGetter extends PTCallable<Network> {
    @Override
    public Network internalRun() {
        return this.task.getDataAccessObject().getWholeNetwork();
    }
}

@Path("all")
public class AllResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Network getAll() {
        return new AllGetter().call();  // No Threads
    }
}

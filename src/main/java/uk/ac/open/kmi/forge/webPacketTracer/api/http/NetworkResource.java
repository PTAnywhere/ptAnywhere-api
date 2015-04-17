package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Network;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

class NetworkGetter extends PTCallable<Network> {
    @Override
    public Network internalRun() {
        return this.connection.getDataAccessObject().getWholeNetwork();
    }
}

public class NetworkResource {

    final UriInfo uri;
    public NetworkResource(UriInfo uri) {
        this.uri = uri;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        final Network network = new NetworkGetter().call();  // No Threads
        return Response.ok(network).link(this.uri.getBaseUri() + "devices", "devices").build();
    }
}

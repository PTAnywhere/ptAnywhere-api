package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Network;
import uk.ac.open.kmi.forge.webPacketTracer.session.SessionManager;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

class NetworkGetter extends PTCallable<Network> {

    public NetworkGetter(SessionManager sm) {
        super(sm);
    }

    @Override
    public Network internalRun() {
        return this.connection.getDataAccessObject().getWholeNetwork();
    }
}

public class NetworkResource {

    final UriInfo uri;
    final SessionManager sm;
    public NetworkResource(UriInfo uri, SessionManager sm) {
        this.uri = uri;
        this.sm = sm;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        final Network network = new NetworkGetter(this.sm).call();  // No Threads
        return addDefaultLinks(Response.ok(network)).build();
    }

    private Response.ResponseBuilder addDefaultLinks(Response.ResponseBuilder rb) {
        final URI sessionURL = Utils.getParent(this.uri.getRequestUri());
        return rb.link(sessionURL, "session").link(sessionURL + "devices", "devices");
    }
}

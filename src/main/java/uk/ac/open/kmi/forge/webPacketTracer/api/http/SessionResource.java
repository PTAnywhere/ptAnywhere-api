package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("sessions/{session}")
public class SessionResource {

    @Context
    UriInfo uri;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSession(
            @PathParam("session") String sessionId) {
        final String requestUri = Utils.getURIWithSlashRemovingQuery(this.uri.getRequestUri());
        return Response.ok("Sample return").
                links(getSessionsLink()).
                link(requestUri + "devices", "devices").
                link(requestUri + "network", "network").build();
    }

    private Link getSessionsLink() {
        return Link.fromUri(Utils.getParent(this.uri.getRequestUri())).rel("collection").build();
    }
}

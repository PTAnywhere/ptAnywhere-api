package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("sessions/{session}")
public class SessionResource {

    @Context
    UriInfo uri;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSession(@PathParam("session") String sessionId) {
        final String requestUri = Utils.getURIWithSlashRemovingQuery(this.uri.getRequestUri());
        return Response.ok("Sample return").
                links(getSessionsLink()).
                link(requestUri + "devices", "devices").
                link(requestUri + "network", "network").build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeDevice(@PathParam("session") String sessionId) {
        if (!sessionId.equals("id4"))
            return Response.noContent().
                    links(getSessionsLink()).build();
        return Response.ok(). // TODO return deleted session.
                links(getSessionsLink()).build();
    }

    private Link getSessionsLink() {
        return Link.fromUri(Utils.getParent(this.uri.getRequestUri())).rel("collection").build();
    }
}

package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.session.SessionManager;
import uk.ac.open.kmi.forge.webPacketTracer.session.SessionsManager;
import static uk.ac.open.kmi.forge.webPacketTracer.api.http.URLFactory.SESSION_PARAM;
import static uk.ac.open.kmi.forge.webPacketTracer.api.http.URLFactory.DEVICE_PATH;
import static uk.ac.open.kmi.forge.webPacketTracer.api.http.URLFactory.NETWORK_PATH;
import static uk.ac.open.kmi.forge.webPacketTracer.api.http.URLFactory.LINKS_PATH;
import static uk.ac.open.kmi.forge.webPacketTracer.api.http.URLFactory.LINK_PARAM;
import static uk.ac.open.kmi.forge.webPacketTracer.api.http.URLFactory.CONTEXT_PATH;

import javax.ws.rs.*;
import javax.ws.rs.core.*;


public class SessionResource {

    final UriInfo uri;
    final SessionsManager sm;
    public SessionResource(UriInfo uri, SessionsManager sm) {
        this.uri = uri;
        this.sm = sm;
    }

    @Path(CONTEXT_PATH)
    public ContextsResource getContextResource(@Context UriInfo u, @PathParam(SESSION_PARAM) String sessionId) {
        return new ContextsResource(u, new SessionManager(sessionId, this.sm));
    }

    @Path(DEVICE_PATH)
    public DevicesResource getDeviceResource(@Context UriInfo u, @PathParam(SESSION_PARAM) String sessionId) {
        return new DevicesResource(u, new SessionManager(sessionId, this.sm));
    }

    @Path(NETWORK_PATH)
    public NetworkResource getNetworkResource(@Context UriInfo u, @PathParam(SESSION_PARAM) String sessionId) {
        return new NetworkResource(u, new SessionManager(sessionId, this.sm));
    }

    @Path(LINKS_PATH + "/{" + LINK_PARAM + "}")
    public LinkResource getLinkResource(@Context UriInfo u, @PathParam(SESSION_PARAM) String sessionId) {
        return new LinkResource(u, new SessionManager(sessionId, this.sm));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSession(@PathParam(SESSION_PARAM) String sessionId) {
        final String requestUri = Utils.getURIWithSlashRemovingQuery(this.uri.getRequestUri());
        if (!this.sm.doesExist(sessionId))
            throw new SessionNotFoundException(sessionId, getSessionsLink());

        return Response.ok("\"" + sessionId + "\"").
                links(getSessionsLink()).
                link(requestUri + DEVICE_PATH, "devices").
                link(requestUri + NETWORK_PATH, "network").build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeDevice(@PathParam(SESSION_PARAM) String sessionId) {
        final String requestUri = Utils.getURIWithSlashRemovingQuery(this.uri.getRequestUri());
        if (this.sm.doesExist(sessionId)) {
            this.sm.deleteSession(sessionId);
            return Response.ok(). // TODO return deleted session.
                    links(getSessionsLink()).build();
        }
        return Response.status(Response.Status.NOT_FOUND).
                links(getSessionsLink()).build();

    }

    private Link getSessionsLink() {
        return Link.fromUri(Utils.getParent(this.uri.getRequestUri())).rel("collection").build();
    }
}

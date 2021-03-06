package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import io.swagger.annotations.*;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.ErrorBean;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.SessionNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionManager;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManager;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.SESSION_PARAM;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.DEVICE_PATH;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.NETWORK_PATH;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.LINKS_PATH;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.LINK_PARAM;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.CONTEXT_PATH;

import javax.ws.rs.*;
import javax.ws.rs.core.*;


@Api(hidden = true)
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
    @ApiOperation(value = "Retrieves information of a session", tags = "session",
            notes = "It returns the identifier of the session in a JSON string format.")
    @ApiResponses(value = {
       @ApiResponse(code=200, message="Returns the id of associated to the session.", response = String.class),
       @ApiResponse(code=SessionNotFoundException.status, response=ErrorBean.class, message=SessionNotFoundException.description)
    })
    public Response getSession(
            @ApiParam(value = "Identifier of the session to be retrieved.") @PathParam(SESSION_PARAM) String sessionId) {
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
    @ApiOperation(value = "Deletes a session", tags = "session")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = SessionNotFoundException.status, response = ErrorBean.class, message = SessionNotFoundException.description)
    })
    public Response removeDevice(
        @ApiParam(value = "Identifier of the session to be deleted.") @PathParam(SESSION_PARAM) String sessionId) {
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

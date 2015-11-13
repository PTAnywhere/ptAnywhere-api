package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import io.swagger.annotations.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecord;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.ErrorBean;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.NoPTInstanceAvailableException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.UnresolvableFileUrlException;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.NewSession;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManager;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.SESSION_PARAM;


@Path(URLFactory.SESSION_PATH)
@Api
@Produces(MediaType.APPLICATION_JSON)
public class SessionsResource {
    @Context
    UriInfo uri;

    @Path("{" + SESSION_PARAM + "}")
    public SessionResource getResource(@Context ServletContext servletContext, @Context UriInfo u) {
        final SessionsManager sm = APIApplication.createSessionsManager(servletContext);
        return new SessionResource(u, sm);
    }

    @GET
    @ApiOperation(value = "Get all the current sessions", tags = "session",
                    response = String.class, responseContainer = "set",
                    notes = "The returned strings correspond to the identifiers of the sessions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@Context ServletContext servletContext) {
        final SessionsManager sm = APIApplication.createSessionsManager(servletContext);
        final Set<String> sessions = sm.getCurrentSessions();
        return Response.ok(Utils.toJsonStringArray(sessions)).links(createLinks(sessions)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve information of the newly created session", tags="session")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Session created successfully. Identifier of the new session.", response = String.class,
                responseHeaders = { @ResponseHeader(name = "location", description = "URL for the newly created session", response=String.class) } ),
        @ApiResponse(code = NoPTInstanceAvailableException.status, response = ErrorBean.class,
                message = NoPTInstanceAvailableException.description),
        @ApiResponse(code = UnresolvableFileUrlException.status, response = ErrorBean.class,
                message = UnresolvableFileUrlException.description)
    })
    // TODO Even better if we use:
    // https://jersey.java.net/documentation/latest/user-guide.html#declarative-linking
    public Response createSession(@Context ServletContext servletContext, @Context HttpServletRequest request,
                                  @ApiParam(value = "Session to be created. <br> 'fileUrl' " +
                                                    "specifies the file to be opened at the beginning.") NewSession newSession)
            throws URISyntaxException, NoPTInstanceAvailableException {
        final String id = APIApplication.createSessionsManager(servletContext).createSession(newSession.getFileUrl());  // May throw NoPTInstanceAvailableException
        final InteractionRecord ir = APIApplication.createInteractionRecord(servletContext, request, id);
        ir.interactionStarted();
        return Response.created(new URI(getSessionRelativeURI(id))).entity(Utils.toJsonString(id)).
                links(getItemLink(id)).build();
    }

    // FIXME Read this: https://jersey.java.net/documentation/latest/user-guide.html#uris-and-links

    private String getSessionRelativeURI(String id) {
        return Utils.getURIWithSlashRemovingQuery(this.uri.getRequestUri()) + Utils.encodeForURL(id);
    }

    private Link getItemLink(String id) {
        return Link.fromUri(getSessionRelativeURI(id)).rel("item").build();
    }

    private Link[] createLinks(Collection<String> sessionIds) {
        final Link[] links = new Link[sessionIds.size()];
        final Iterator<String> devIt = sessionIds.iterator();
        for(int i=0; i<links.length; i++) {
            links[i] = getItemLink(devIt.next());
        }
        return links;
    }
}

package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecord;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.exceptions.NoPTInstanceAvailableException;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManager;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.SESSION_PARAM;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;


@Path(URLFactory.SESSION_PATH)
public class SessionsResource {
    @Context
    UriInfo uri;

    final SessionsManager sm = SessionsManager.create();

    @Path("{" + SESSION_PARAM + "}")
    public SessionResource getResource(@Context UriInfo u) {
        return new SessionResource(u, sm);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        final Set<String> sessions = this.sm.getCurrentSessions();
        return Response.ok(Utils.toJsonStringArray(sessions)).links(createLinks(sessions)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    // TODO Even better if we use:
    // https://jersey.java.net/documentation/latest/user-guide.html#declarative-linking
    public Response createSession(@Context ServletContext servletContext) throws URISyntaxException, NoPTInstanceAvailableException {
        final String id = this.sm.createSession();  // May throw NoPTInstanceAvailableException
        final InteractionRecord ir = APIApplication.createInteractionRecord(servletContext);
        ir.interactionStarted(id);
        return Response.created(new URI(getSessionRelativeURI(id))).
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

package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.session.BusyInstancesException;
import uk.ac.open.kmi.forge.webPacketTracer.session.SessionManager;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;


@Path("sessions")
public class SessionsResource {
    @Context
    UriInfo uri;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        final SessionManager sm = SessionManager.create();
        final Set<String> sessions = sm.getCurrentSessions();
        return Response.ok(Utils.toJsonStringArray(sessions)).links(createLinks(sessions)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSession() throws URISyntaxException {
        final SessionManager sm = SessionManager.create();
        try {
            final String id = sm.createSession();
            return Response.created(new URI(getSessionRelativeURI(id))).
                    links(getItemLink(id)).build();
        } catch(BusyInstancesException e) {
            return Response.serverError().build();
        }
    }
}

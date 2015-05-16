package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.analytics.InteractionRecordFactory;
import uk.ac.open.kmi.forge.webPacketTracer.analytics.InteractionRecordable;
import uk.ac.open.kmi.forge.webPacketTracer.session.BusyInstancesException;
import uk.ac.open.kmi.forge.webPacketTracer.session.SessionsManager;

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

    final SessionsManager sm = SessionsManager.create();

    @Path("{" + SessionResource.SESSION_PARAM + "}")
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
    public Response createSession() throws URISyntaxException {
        try {
            final String id = this.sm.createSession();
            final InteractionRecordable ir = InteractionRecordFactory.create();
            ir.interactionStarted();
            return Response.created(new URI(getSessionRelativeURI(id))).
                    links(getItemLink(id)).build();
        } catch(BusyInstancesException e) {
            return Response.serverError().build();
        }
    }

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

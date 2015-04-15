package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


@Path("sessions")
public class SessionsResource {
    @Context
    UriInfo uri;

    private Set<String> getSessionIds() {
        final Set<String> ret = new HashSet<String>();
        ret.add("id1");
        ret.add("id2");
        ret.add("id3");
        return ret;
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        final Set<String> sessions = getSessionIds();
        return Response.ok(Utils.toJsonStringArray(sessions)).links(createLinks(sessions)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSession() throws URISyntaxException {
        final String id = "id4";
        return Response.created(new URI(getSessionRelativeURI(id))).
                links(getItemLink(id)).build();
    }
}

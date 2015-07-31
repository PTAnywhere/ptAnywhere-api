package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import uk.ac.open.kmi.forge.ptAnywhere.gateway.PTCallable;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.InnerLink;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.Link;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionManager;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.DEVICE_PATH;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.LINK_PARAM;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;


class LinkGetter extends PTCallable<InnerLink> {
    final String linkId;
    public LinkGetter(SessionManager sm, String linkId) {
        super(sm);
        this.linkId = linkId;
    }
    @Override
    public InnerLink internalRun() {
        return this.connection.getDataAccessObject().getLink(this.linkId);
    }
}


public class LinkResource {

    UriInfo uri;
    SessionManager sm;
    public LinkResource(UriInfo uri, SessionManager sm) {
        this.uri = uri;
        this.sm = sm;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLink(@PathParam(LINK_PARAM) String linkId) {
        final InnerLink l = new LinkGetter(this.sm, linkId).call();  // Same thread
        if (l==null) {
            return addDefaultLinks(Response.noContent()).build();
        }

        final URLFactory uf = new URLFactory(this.uri.getBaseUri(), sm.getSessionId());
        final Link rl = Link.createFromInnerLink(l, uf);

        Response.ResponseBuilder ret = addDefaultLinks(Response.ok());
        for (String endpointURL: rl.getEndpoints()) {
            ret = ret.link(endpointURL, "endpoint");
        }

        return ret.entity(rl).build();
    }

    private URI getSessionURL() {
        return Utils.getParent(Utils.getParent(this.uri.getRequestUri()));
    }

    private Response.ResponseBuilder addDefaultLinks(Response.ResponseBuilder rb) {
        final URI sessionURL = getSessionURL();
        return rb.link(sessionURL, "session").link(sessionURL + DEVICE_PATH, "devices");
    }

    // From the API perspective, the best thing would be to place the DELETE here.
    // However, seeing how the PT library (or even the protocol) works, I will keep it in the endpoints.
}

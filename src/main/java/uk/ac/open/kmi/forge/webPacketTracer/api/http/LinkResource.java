package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.InnerLink;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.RefactoredLink;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


class LinkGetter extends PTCallable<InnerLink> {
    final String linkId;
    public LinkGetter(String linkId) {
        this.linkId = linkId;
    }
    @Override
    public InnerLink internalRun() {
        getLog().error(this.linkId);
        return this.connection.getDataAccessObject().getLink(this.linkId);
    }
}

@Path("links/{link}")
public class LinkResource {
    @Context
    UriInfo uri;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLink(@PathParam("link") String linkId) {
        final InnerLink l = new LinkGetter(linkId).call();  // Same thread
        if (l==null)
            return Response.noContent().build();

        final RefactoredLink rl = new RefactoredLink();
        rl.setId(l.getId());
        for (String[] endpoint: l.getEndpoints()) {
            rl.appendEndpoint(getPortURL(endpoint));
        }

        return Response.ok(rl).build();
    }

    private String getPortURL(String[] endpointInfo) {
        return this.uri.getBaseUri() +
                "devices/" + endpointInfo[0] +
                "/ports/" + Utils.escapePort(endpointInfo[1]);
    }
}

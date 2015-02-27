package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Link;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


class LinkGetter extends  PTCallable<Link> {
    final String deviceId;
    final String portName;
    public LinkGetter(String deviceId, String portName) {
        this.deviceId = deviceId;
        this.portName = portName;
    }
    @Override
    public Link internalRun() {
        return this.connection.getDataAccessObject().getLink(this.deviceId, this.portName);
    }
}

class LinkDeleter extends PTCallable<Link> {
    final Link toDelete = new Link();
    public LinkDeleter(String deviceId, String portName) {
        this.toDelete.setToDevice(deviceId);
        this.toDelete.setToPort(portName);
    }
    @Override
    public Link internalRun() {
        final boolean success = this.connection.getDataAccessObject().removeLink(this.toDelete.getToDevice(), this.toDelete.getToPort());
        if (success)
            return this.toDelete;
        return null;
    }
}

class LinkCreator extends PTCallable<Link> {
    final String deviceId;
    final String portName;
    final Link linkToCreate;
    public LinkCreator(String deviceId, String portName, Link linkToCreate) {
        this.deviceId = deviceId;
        this.portName = portName;
        this.linkToCreate = linkToCreate;
    }
    @Override
    public Link internalRun() {
        final boolean success = this.connection.getDataAccessObject().createLink(this.deviceId, this.portName, this.linkToCreate);
        if (success)
            // Improvable performance
            return this.connection.getDataAccessObject().getLink(this.deviceId, this.portName);
        return null;
    }
}

@Path("devices/{device}/ports/{port}/link")
public class LinkResource {
    @Context
    UriInfo uri;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLink(@PathParam("device") String deviceId,
                          @PathParam("port") String portName) {
        final Link l = new LinkGetter(deviceId, Utils.unescapePort(portName)).call();
        if (l==null)
            return Response.noContent().
                    links(getPortLink()).build();
        return Response.ok(l).
                links(getPortLink()).
                links(getToPortLink(l)).build();
    }
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeLink(@PathParam("device") String deviceId,
                             @PathParam("port") String portName) {
        final Link deletedLink = new LinkDeleter(deviceId, Utils.unescapePort(portName)).call();
        if (deletedLink==null)
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(deletedLink).
                    links(getPortLink()).build();
        return Response.ok(deletedLink).
                links(getPortLink()).build();
    }
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLink(Link newLink,
                             @PathParam("device") String deviceId,
                             @PathParam("port") String portName) {
        final Link createdLink = new LinkCreator(deviceId, Utils.unescapePort(portName), newLink).call();
        if (createdLink==null)
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(newLink).
                    links(getPortLink()).build();
        return Response.created(this.uri.getRequestUri()).entity(createdLink).
                links(getPortLink()).
                links(getToPortLink(createdLink)).build();
    }
    private javax.ws.rs.core.Link getPortLink() {
        return javax.ws.rs.core.Link.fromUri(Utils.getParent(this.uri.getRequestUri())).rel("port").build();  // Rename it to from?
    }
    private javax.ws.rs.core.Link getToPortLink(Link l) {
        return javax.ws.rs.core.Link.fromUri(
                    // FIXME This URL does not exist yet (and maybe it's better to return the id based one)
                    this.uri.getBaseUri() +
                    "devices/" + l.getToDevice() +
                    "/ports/" + Utils.escapePort(l.getToPort()) + "?byName=true"
                ).rel("to").build();  // Rename it to port?
    }
}

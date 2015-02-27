package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Link;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;


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

class LinkDeleter extends PTCallable<Response> {
    final Link toDelete = new Link();
    public LinkDeleter(String deviceId, String portName) {
        this.toDelete.setToDevice(deviceId);
        this.toDelete.setToPort(portName);
    }
    @Override
    public Response internalRun() {
        final boolean success = this.connection.getDataAccessObject().removeLink(this.toDelete.getToDevice(), this.toDelete.getToPort());
        if (success)
            return Response.ok().entity(this.toDelete).build();
        else
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(this.toDelete).build();
    }
}

class LinkCreator extends PTCallable<Response> {
    final String deviceId;
    final String portName;
    final Link linkToCreate;
    final URI requestUri;
    public LinkCreator(String deviceId, String portName, Link linkToCreate, URI requestUri) {
        this.deviceId = deviceId;
        this.portName = portName;
        this.linkToCreate = linkToCreate;
        this.requestUri = requestUri;
    }
    @Override
    public Response internalRun() {
        final boolean success = this.connection.getDataAccessObject().createLink(this.deviceId, this.portName, this.linkToCreate);
        if (success) {
            final Link link = this.connection.getDataAccessObject().getLink(this.deviceId, this.portName);
            return Response.created(this.requestUri).entity(link).build();
        } else
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(this.linkToCreate).build();  // Improvable performance
    }
}

@Path("devices/{device}/ports/{port}/link")
public class LinkResource {
    @Context
    UriInfo uri;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Link getLink(@PathParam("device") String deviceId,
                          @PathParam("port") String portName) {
        return new LinkGetter(deviceId, Utils.unescapePort(portName)).call();
    }
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeLink(@PathParam("device") String deviceId,
                             @PathParam("port") String portName) {
        return new LinkDeleter(deviceId, Utils.unescapePort(portName)).call();
    }
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLink(Link newLink,
                             @PathParam("device") String deviceId,
                             @PathParam("port") String portName) {
        return new LinkCreator(deviceId, Utils.unescapePort(portName), newLink, this.uri.getRequestUri()).call();
    }
}

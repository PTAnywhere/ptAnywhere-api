package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Link;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


class LinkGetter extends  PTCallable<Link> {
    final String deviceId;
    final String portName;
    public LinkGetter(String deviceId, String portName) {
        this.deviceId = deviceId;
        this.portName = portName;
    }
    @Override
    public Link internalRun() {
        return this.task.getDataAccessObject().getLink(this.deviceId, this.portName);
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
        final boolean success = this.task.getDataAccessObject().removeLink(this.toDelete.getToDevice(), this.toDelete.getToPort());
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
    public LinkCreator(String deviceId, String portName, Link linkToCreate) {
        this.deviceId = deviceId;
        this.portName = portName;
        this.linkToCreate = linkToCreate;
    }
    @Override
    public Response internalRun() {
        final boolean success = this.task.getDataAccessObject().createLink(this.deviceId, this.portName, this.linkToCreate);
        if (success)
            return Response.ok().entity(this.task.getDataAccessObject().getLink(this.deviceId, this.portName)).build();
        else
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(this.linkToCreate).build();  // Improvable performance
    }
}

@Path("devices/{device}/ports/{port}/link")
public class LinkResource {
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
        return new LinkCreator(deviceId, Utils.unescapePort(portName), newLink).call();
    }
}

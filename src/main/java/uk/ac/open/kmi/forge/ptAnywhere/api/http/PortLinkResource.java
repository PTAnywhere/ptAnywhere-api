package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import io.swagger.annotations.*;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecord;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.ErrorBean;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.LinkNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.PacketTracerConnectionException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.SessionNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.gateway.PTCallable;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.HalfLink;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.InnerLink;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.Link;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionManager;

import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.PORT_PARAM;
import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.DEVICE_PARAM;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;


abstract class AbstractPortLinkHandler extends  PTCallable<Link> {
    final String deviceId;
    final String portName;
    final URLFactory uf;
    public AbstractPortLinkHandler(SessionManager sm, String deviceId, String portName, URI baseURI) {
        super(sm);
        this.deviceId = deviceId;
        this.portName = portName;
        this.uf = new URLFactory(baseURI, sm.getSessionId(), deviceId);
    }
    @Override
    public Link internalRun() {
        final InnerLink il = handleLink();
        if (il==null) return null;
        return Link.createFromInnerLink(il, this.uf);
    }
    abstract InnerLink handleLink();
}

class PortLinkGetter extends AbstractPortLinkHandler {
    public PortLinkGetter(SessionManager sm, String deviceId, String portName, URI baseURI) {
        super(sm, deviceId, portName, baseURI);
    }
    @Override
    public InnerLink handleLink() {
        return this.connection.getDataAccessObject().getLink(this.deviceId, this.portName);
    }
}

class LinkDeleter extends AbstractPortLinkHandler {
    public LinkDeleter(SessionManager sm, String deviceId, String portName, URI baseURI) {
        super(sm, deviceId, portName, baseURI);
    }
    @Override
    public InnerLink handleLink() {
        final InnerLink il = this.connection.getDataAccessObject().getLink(this.deviceId, this.portName);
        final boolean success = this.connection.getDataAccessObject().removeLink(this.deviceId, this.portName);
        if (success)
            return il;
        return null;
    }
}

class LinkCreator extends AbstractPortLinkHandler {
    final HalfLink linkToCreate;
    public LinkCreator(SessionManager sm, String deviceId, String portName, HalfLink linkToCreate, URI baseURI) {
        super(sm, deviceId, portName, baseURI);
        this.linkToCreate = linkToCreate;
    }
    @Override
    public InnerLink handleLink() {
        final boolean success = this.connection.getDataAccessObject().createLink(this.deviceId, this.portName, this.linkToCreate);
        if (success)
            // Improvable performance
            return this.connection.getDataAccessObject().getLink(this.deviceId, this.portName);
        return null;
    }
}


@Api(hidden = true)
public class PortLinkResource {

    final UriInfo uri;
    final SessionManager sm;

    public PortLinkResource(UriInfo uri, SessionManager sm) {
        this.uri = uri;
        this.sm = sm;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieves the details of the connection (i.e., the link) of the port", response = Link.class,
                    tags = "device")
    @ApiResponses(value = {
            @ApiResponse(code = LinkNotFoundException.status, response = ErrorBean.class, message = LinkNotFoundException.description),
            @ApiResponse(code = PacketTracerConnectionException.status, response = ErrorBean.class, message = PacketTracerConnectionException.description),
            @ApiResponse(code = SessionNotFoundException.status, response = ErrorBean.class, message = SessionNotFoundException.description)
    })
    public Response getLink(@ApiParam(value = "Identifier of the device.") @PathParam(DEVICE_PARAM) String deviceId,
                            @ApiParam(value = "Name of the port inside the device.") @PathParam(PORT_PARAM) String portName) {
        final Link l = new PortLinkGetter(this.sm, deviceId, Utils.unescapePort(portName), this.uri.getBaseUri()).call();
        // TODO add links to not found exception
        return Response.ok(l).
                links(getPortLink()).build();
                // TODO create endpoints links
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Disconnects a port (i.e., destroys its link)", response = Link.class, tags = "device")
    @ApiResponses(value = {
            @ApiResponse(code = LinkNotFoundException.status, response = ErrorBean.class, message = LinkNotFoundException.description),
            @ApiResponse(code = 500, response = ErrorBean.class, message = "The link could not be removed"),
            @ApiResponse(code = PacketTracerConnectionException.status, response = ErrorBean.class, message = PacketTracerConnectionException.description),
            @ApiResponse(code = SessionNotFoundException.status, response = ErrorBean.class, message = SessionNotFoundException.description)
    })
    public Response removeLink(@ApiParam(value = "Identifier of the device.") @PathParam(DEVICE_PARAM) String deviceId,
                               @ApiParam(value = "Name of the port inside the device.") @PathParam(PORT_PARAM) String portName,
                               @Context ServletContext servletContext) {
        final Link deletedLink = new LinkDeleter(this.sm, deviceId, Utils.unescapePort(portName), this.uri.getBaseUri()).call();
        // TODO add links to not found exception
        if (deletedLink==null)  // It exists, couldn't be removed
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(deletedLink).
                    links(getPortLink()).build();
        final InteractionRecord ir =  APIApplication.createInteractionRecord(servletContext, this.sm.getSessionId());
        ir.deviceDisconnected(deletedLink.getUrl(), deletedLink.getEndpoints());
        return Response.ok(deletedLink).
                links(getPortLink()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Connects the port to other (i.e., it creates a link)", response = Link.class, tags = "device")
    @ApiResponses(value = {
            @ApiResponse(code = LinkNotFoundException.status, response = ErrorBean.class, message = LinkNotFoundException.description),
            @ApiResponse(code = 406, response = ErrorBean.class, message = "There link could not be created."),
            @ApiResponse(code = PacketTracerConnectionException.status, response = ErrorBean.class, message = PacketTracerConnectionException.description),
            @ApiResponse(code = SessionNotFoundException.status, response = ErrorBean.class, message = SessionNotFoundException.description)
    })
    public Response createLink(@ApiParam(value = "The other endpoint of the link. " +
                                                    "Only the 'toPort' field will be considered.") HalfLink newLink,
                               @ApiParam(value = "Identifier of the device.") @PathParam(DEVICE_PARAM) String deviceId,
                               @ApiParam(value = "Name of the port inside the device.") @PathParam(PORT_PARAM) String portName,
                               @Context ServletContext servletContext) {
        final Link createdLink = new LinkCreator(this.sm, deviceId, Utils.unescapePort(portName), newLink, this.uri.getBaseUri()).call();
        if (createdLink==null)
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(newLink).
                    links(getPortLink()).build();
        final InteractionRecord ir =  APIApplication.createInteractionRecord(servletContext, sm.getSessionId());
        ir.deviceConnected(createdLink.getUrl(), createdLink.getEndpoints());
        return Response.created(this.uri.getRequestUri()).entity(createdLink).
                links(getPortLink()).build();
                // TODO create endpoints links
    }
    private javax.ws.rs.core.Link getPortLink() {
        return javax.ws.rs.core.Link.fromUri(Utils.getParent(this.uri.getRequestUri())).rel("port").build();  // Rename it to from?
    }
}

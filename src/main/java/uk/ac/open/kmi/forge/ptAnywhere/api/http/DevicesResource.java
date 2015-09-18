package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import io.swagger.annotations.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecord;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.ErrorBean;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.PacketTracerConnectionException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.SessionNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.gateway.PTCallable;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.Device;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionManager;

import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.DEVICE_PARAM;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


class DevicesGetter extends PTCallable<Collection<Device>> {

    final URLFactory uf;

    public DevicesGetter(SessionManager sm, URI baseURI) {
        super(sm);
        this.uf = new URLFactory(baseURI, sm.getSessionId());
    }

    @Override
    public Collection<Device> internalRun() {
        final Collection<Device> ret = this.connection.getDataAccessObject().getDevices();
        for(Device d: ret) {
            d.setURLFactory(this.uf);
        }
        return ret;
    }
}

class DevicePoster extends PTCallable<Device> {

    final Device d;
    final URLFactory uf;

    public DevicePoster(SessionManager sm, Device d, URI baseURI) {
        super(sm);
        this.d = d;
        this.uf = new URLFactory(baseURI, sm.getSessionId());
    }

    @Override
    public Device internalRun() {
        final Device ret = this.connection.getDataAccessObject().createDevice(this.d);
        if (ret!=null) ret.setURLFactory(this.uf);
        return ret;
    }
}


@Api(hidden = true, tags = "network")
public class DevicesResource {

    private static final Log LOGGER = LogFactory.getLog(DevicesResource.class);

    final UriInfo uri;
    final URLFactory gen;
    final SessionManager sm;

    public DevicesResource(UriInfo uri, SessionManager sm) {
        this.uri = uri;
        this.sm = sm;
        this.gen = new URLFactory(uri.getBaseUri(), this.sm.getSessionId());
    }

    @Path("{" + DEVICE_PARAM + "}")
    public DeviceResource getResource(@Context UriInfo u) {
        return new DeviceResource(u, this.sm);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new device")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Device created successfully",
                responseHeaders = { @ResponseHeader(name = "location", description = "URL for the newly created device", response=String.class) } ),
        @ApiResponse(code = PacketTracerConnectionException.status, response = ErrorBean.class, message = PacketTracerConnectionException.description),
        @ApiResponse(code = SessionNotFoundException.status, response = ErrorBean.class, message = SessionNotFoundException.description)
    })
    public Response createDevice(@Context ServletContext servletContext, @Context HttpServletRequest request,
            @ApiParam(value = "Device to be created. <br> 'port' and 'url' fields are not expected " +
                            "to be completed and therefore they will be ignored.") Device newDevice)
            throws URISyntaxException {
        final Device device = new DevicePoster(this.sm, newDevice, this.uri.getBaseUri()).call();
        if (device==null)
            return addDefaultLinks(Response.status(Response.Status.BAD_REQUEST).entity(newDevice)).build();

        final InteractionRecord ir = APIApplication.createInteractionRecord(servletContext, request, sm.getSessionId());
        final String newDeviceUri = this.gen.createDeviceURL(device.getId());
        ir.deviceCreated(newDeviceUri, device.getLabel(), device.getGroup());
        return addDefaultLinks(Response.created(new URI(newDeviceUri))).
                entity(device).
                links(getItemLink(device.getId())).build();  // Not using a new Thread
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieves all the existing devices", response = Device.class, responseContainer = "set")
    @ApiResponses(value = {
        @ApiResponse(code = PacketTracerConnectionException.status, response = ErrorBean.class, message = PacketTracerConnectionException.description),
        @ApiResponse(code = SessionNotFoundException.status, response = ErrorBean.class, message = SessionNotFoundException.description)
    })
    public Response getJson() {
        final Collection<Device> d = new DevicesGetter(this.sm, this.uri.getBaseUri()).call();  // Not using a new Thread
        // To array because otherwise Response does not know how to serialize Collection<Device>
        return addDefaultLinks(Response.ok(d.toArray(new Device[d.size()]))).
                links(createLinks(d)).build();  // Not using a new Thread
    }

    private Response.ResponseBuilder addDefaultLinks(Response.ResponseBuilder rb) {
        final URI sessionURL = Utils.getParent(this.uri.getRequestUri());
        return rb.link(sessionURL, "session").link(sessionURL + "network", "network");
    }

    private Link getItemLink(String id) {
        return Link.fromUri(this.gen.createDeviceURL(id)).rel("item").build();
    }

    private Link[] createLinks(Collection<Device> devices) {
        final Link[] links = new Link[devices.size()];
        final Iterator<Device> devIt = devices.iterator();
        for(int i=0; i<links.length; i++) {
            links[i] = getItemLink(devIt.next().getId());
        }
        return links;
    }
}

package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecord;
import uk.ac.open.kmi.forge.ptAnywhere.gateway.PTCallable;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.Device;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionManager;

import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.DEVICE_PARAM;

import javax.servlet.ServletContext;
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
    public Response createDevice(Device newDevice,
                                 @Context ServletContext servletContext) throws URISyntaxException {
        final Device device = new DevicePoster(this.sm, newDevice, this.uri.getBaseUri()).call();
        if (device==null)
            return addDefaultLinks(Response.status(Response.Status.BAD_REQUEST).entity(newDevice)).build();

        final InteractionRecord ir = APIApplication.createInteractionRecord(servletContext);
        final String newDeviceUri = this.gen.createDeviceURL(device.getId());
        ir.deviceCreated(sm.getSessionId(), newDeviceUri, device.getLabel(), device.getGroup());
        return addDefaultLinks(Response.created(new URI(newDeviceUri))).
                entity(device).
                links(getItemLink(device.getId())).build();  // Not using a new Thread
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
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

package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.analytics.InteractionRecord;
import uk.ac.open.kmi.forge.webPacketTracer.analytics.InteractionRecordFactory;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.session.SessionManager;

import static uk.ac.open.kmi.forge.webPacketTracer.api.http.URLFactory.DEVICE_PARAM;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


class DevicesGetter extends PTCallable<Collection<Device>> {

    public DevicesGetter(SessionManager sm) {
        super(sm);
    }

    @Override
    public Collection<Device> internalRun() {
        return this.connection.getDataAccessObject().getDevices();
    }
}

class DevicePoster extends PTCallable<Device> {
    final Device d;

    public DevicePoster(SessionManager sm, Device d) {
        super(sm);
        this.d = d;
    }

    @Override
    public Device internalRun() {
        return this.connection.getDataAccessObject().createDevice(this.d);
    }
}

public class DevicesResource {

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
    public Response createDevice(Device newDevice) throws URISyntaxException {
        final Device device = new DevicePoster(this.sm, newDevice).call();
        if (device==null)
            return addDefaultLinks(Response.status(Response.Status.BAD_REQUEST).entity(newDevice)).build();
        final InteractionRecord ir = InteractionRecordFactory.create();
        final String newDeviceUri = this.gen.createDeviceURL(device.getId());
        ir.deviceCreated(sm.getSessionId(), newDeviceUri, device.getLabel(), device.getGroup());
        return addDefaultLinks(Response.created(new URI(newDeviceUri))).
                entity(device).
                links(getItemLink(device.getId())).build();  // Not using a new Thread
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJson() {
        final Collection<Device> d = new DevicesGetter(this.sm).call();  // Not using a new Thread
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

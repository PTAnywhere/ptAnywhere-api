package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


class DevicesGetter extends PTCallable<Collection<Device>> {
    @Override
    public Collection<Device> internalRun() {
        return this.connection.getDataAccessObject().getDevices();
    }
}

class DevicePoster extends PTCallable<Device> {
    final Device d;
    public DevicePoster(Device d) {
        this.d = d;
    }
    @Override
    public Device internalRun() {
        return this.connection.getDataAccessObject().createDevice(this.d);
    }
}

public class DevicesResource {

    final UriInfo uri;
    public DevicesResource(UriInfo uri) {
        this.uri = uri;
    }

    @Path("{" + DeviceResource.DEVICE_PARAM + "}")
    public DeviceResource getResource(@Context UriInfo u) {
        return new DeviceResource(u);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDevice(Device newDevice) throws URISyntaxException{
        final Device device = new DevicePoster(newDevice).call();
        if (device==null)
            return Response.status(Response.Status.BAD_REQUEST).entity(newDevice).
                    links(getNetworkLink()).build();
        return Response.created(new URI(getDeviceRelativeURI(device.getId())))
                .entity(device).links(getNetworkLink()).
                links(getItemLink(device.getId())).build();  // Not using a new Thread
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJson() {
        final Collection<Device> d = new DevicesGetter().call();  // Not using a new Thread
        // To array because otherwise Response does not know how to serialize Collection<Device>
        return Response.ok(d.toArray(new Device[d.size()])).
                links(getNetworkLink()).
                links(createLinks(d)).build();  // Not using a new Thread
    }

    private Link getNetworkLink() {
        return Link.fromUri(this.uri.getBaseUri() + "network").rel("network").build();
    }

    private String getDeviceRelativeURI(String id) {
        return Utils.getURIWithSlashRemovingQuery(this.uri.getRequestUri()) + Utils.encodeForURL(id);
    }

    private Link getItemLink(String id) {
        return Link.fromUri(getDeviceRelativeURI(id)).rel("item").build();
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

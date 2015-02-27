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

@Path("devices")
public class DevicesResource {
    @Context
    UriInfo uri;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDevice(Device newDevice) throws URISyntaxException{
        final Device device = new DevicePoster(newDevice).call();
        if (device==null)
            return Response.status(Response.Status.BAD_REQUEST).entity(newDevice).
                    links(createNetworkLink()).build();
        return Response.created(new URI(getDeviceRelativeURI(device.getId())))
                .entity(device).links(createNetworkLink()).
                links(createItemLink(device.getId())).build();  // Not using a new Thread
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJson() {
        final Collection<Device> d = new DevicesGetter().call();  // Not using a new Thread
        // To array because otherwise Response does not know how to serialize Collection<Device>
        return Response.ok(d.toArray(new Device[d.size()])).
                links(createNetworkLink()).
                links(createLinks(d)).build();  // Not using a new Thread
    }

    private Link createNetworkLink() {
        return Link.fromUri(this.uri.getBaseUri() + "network").rel("network").build();
    }

    private String getDeviceRelativeURI(String id) {
        return this.uri.getRequestUri() + Utils.escapeIdentifier(id);
    }

    private Link createItemLink(String id) {
        return Link.fromUri(getDeviceRelativeURI(id)).rel("item").build();
    }

    private Link[] createLinks(Collection<Device> devices) {
        final Link[] links = new Link[devices.size()];
        final Iterator<Device> devIt = devices.iterator();
        for(int i=0; i<links.length; i++) {
            links[i] = createItemLink(devIt.next().getId());
        }
        return links;
    }
}

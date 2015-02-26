package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.enums.DeviceType;
import com.cisco.pt.ipc.ui.LogicalWorkspace;
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
            return Response.status(Response.Status.BAD_REQUEST).entity(newDevice).build();
        return Response.created(new URI(getDeviceRelativeURI(device.getId()))).entity(device).build();  // Not using a new Thread
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJson() {
        final Collection<Device> d = new DevicesGetter().call();  // Not using a new Thread
        // To array because otherwise Response does not know how to serialize Collection<Device>
        return Response.ok(d.toArray(new Device[d.size()])).
                link(this.uri.getBaseUri() + "network", "network").
                links(createLinks(d)).build();  // Not using a new Thread
    }

    private Link fromRelative(String deviceId, String relType) {
        return Link.fromUri(getDeviceRelativeURI(deviceId)).rel(relType).build();
    }

    private String getDeviceRelativeURI(String id) {
        return this.uri.getRequestUri() + Utils.escapeIdentifier(id);
    }

    private Link[] createLinks(Collection<Device> devices) {
        final Link[] links = new Link[devices.size()];
        final Iterator<Device> devIt = devices.iterator();
        for(int i=0; i<links.length; i++) {
            links[i] = fromRelative(devIt.next().getId(), "item");
        }
        return links;
    }
}

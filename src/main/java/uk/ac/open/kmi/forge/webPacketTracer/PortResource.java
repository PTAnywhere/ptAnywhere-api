package uk.ac.open.kmi.forge.webPacketTracer;

import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


class PortGetter extends PTCallable<Port> {
    final String deviceId;
    final String portName;
    public PortGetter(String deviceId, String portName) {
        this.deviceId = deviceId;
        this.portName = portName;
    }
    @Override
    public Port internalRun() {
        return this.task.getDataAccessObject().getPort(this.deviceId, this.portName);
    }
}

class PortModifier extends PTCallable<Port> {
    final String deviceId;
    final Port modification;
    public PortModifier(String deviceId, Port modification) {
        this.deviceId = deviceId;
        this.modification = modification;
    }

    @Override
    public Port internalRun() {
        return this.task.getDataAccessObject().modifyPort(this.deviceId, this.modification);
    }
}

@Path("devices/{device}/ports/{port}")
public class PortResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Port getPort(
            @PathParam("device") String deviceId,
            @PathParam("port") String portName) {
        return new PortGetter(deviceId, Utils.unescapePort(portName)).call();  // Not using a new Thread
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyPort(
            Port modification,
            @PathParam("device") String deviceId,
            @PathParam("port") String portName) {
        if (modification.getPortName()==null) {
            modification.setPortName(Utils.unescapePort(portName));
            final Port ret = new PortModifier(deviceId, modification).call();  // Not using a new Thread
            return Response.ok().entity(ret).build();
        } else // The portName is provided in the URL, not in the body (i.e., JSON sent).
            return Response.status(Response.Status.BAD_REQUEST).entity(modification).build();
            // throw new BadRequestException(); //Returns HTML
    }
}

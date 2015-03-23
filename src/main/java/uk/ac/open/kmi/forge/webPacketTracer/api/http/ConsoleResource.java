package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import com.cisco.pt.ipc.enums.DeviceType;
import com.cisco.pt.ipc.sim.Device;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.*;


class CommandLineGetter extends PTCallable<Boolean> {
    final String dId;
    public CommandLineGetter(String dId) {
        this.dId = dId;
    }
    @Override
    public Boolean internalRun() {
        final Device d = this.connection.getDataAccessObject().getSimDeviceById(this.dId);
        return d.getCommandLine()!=null; // if not null, it has a console.
        /*if (DeviceType.PC.equals(d.getType()) || DeviceType.SWITCH.equals(d.getType()) ||
                DeviceType.ROUTER.equals(d.getType())) {*/
    }
}

@Path("devices/{device}/console")
public class ConsoleResource {

    @Context
    ServletContext ctx;

    boolean deviceHasCommandLine(String deviceId) {
        return new CommandLineGetter(deviceId).call();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream getDevice(@PathParam("device") String deviceId) {
        if (deviceHasCommandLine(deviceId))
            return this.ctx.getResourceAsStream("widget/console.html");
        else
            throw new InternalServerErrorException("This device does not have command line.");
    }
}
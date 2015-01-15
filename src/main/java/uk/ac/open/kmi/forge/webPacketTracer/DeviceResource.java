package uk.ac.open.kmi.forge.webPacketTracer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by agg96 on 15/01/2015.
 */
@Path("devices/{device}")
public class DeviceResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDevice(@PathParam("device") String deviceName) {
        return "One device";
    }
}

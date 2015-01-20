package uk.ac.open.kmi.forge.webPacketTracer;

import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("devices")
public class DevicesResource {

    private PtSmith ptSmith = null;

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
        if (ptSmith == null) {
            ptSmith = new PtSmith();
        }
        ptSmith.run();
        String devices = ptSmith.getDevicesJson();
        String edges = ptSmith.getEdgesJson();
        return "{ \n \t\"devices\":" + devices + ",\n \t\"edges\":" + edges + "\n}";
    }
}

package uk.ac.open.kmi.forge.webPacketTracer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("all")
public class AllResource {

    private PtSmith ptSmith = null;

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

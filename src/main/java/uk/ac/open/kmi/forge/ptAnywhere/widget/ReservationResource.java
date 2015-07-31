package uk.ac.open.kmi.forge.ptAnywhere.widget;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;


@Path("index.html")
public class ReservationResource extends CustomAbstractResource {
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getWidget() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("title", getApplicationTitle());
        return Response.ok(getPreFilled("/reservation.ftl", map)).
                link(getAPIURL(), "api").build();
    }
}
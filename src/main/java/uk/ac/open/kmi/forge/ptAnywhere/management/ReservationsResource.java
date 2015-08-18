package uk.ac.open.kmi.forge.ptAnywhere.management;


import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;


@Path("backend.html")
public class ReservationsResource extends CustomAbstractResource {
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getWidget() {
        final SessionsManager sm = SessionsManager.create();
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sessions", sm.getCurrentSessions());
        map.put("instances", sm.getAllInstances());
        return Response.ok(getPreFilled("/list_reserves.ftl", map)).
                link(getAPIURL(), "api").build();
    }
}
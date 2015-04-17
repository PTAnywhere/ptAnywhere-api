package uk.ac.open.kmi.forge.webPacketTracer.widget;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;


@Path("p/{session}")
public class WidgetResource extends CustomAbstractResource {

    static String RELATIVE_ROOT_PATH = "../";

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getWidget(@PathParam("session") String sessionId) {
        final String apiUrl = getAppRootURL().toString() + "api/";
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("title", getApplicationTitle());
        map.put("session_api", getAPIURL() + "sessions/" + sessionId);
        return Response.ok(getPreFilled("/widget.ftl", map)).
                link(getAPIURL(), "api").build();
    }
}
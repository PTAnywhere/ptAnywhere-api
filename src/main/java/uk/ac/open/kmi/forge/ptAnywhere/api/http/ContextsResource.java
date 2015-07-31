package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionManager;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory.CONTEXT_DEVICE_PATH;


public class ContextsResource {

    final URLFactory gen;
    public ContextsResource(UriInfo uri, SessionManager sm) {
        this.gen = new URLFactory(uri.getBaseUri(), sm.getSessionId());
    }

    @GET @Path(CONTEXT_DEVICE_PATH)
    @Produces("application/ld+json")
    public Response getJson() throws JSONException {
        //return Response.ok(new Device()).build();
        final String base = "http://schema.org/";
        final JSONObject device = new JSONObject()
                .put("@vocab", gen.getDevicesURL())
                .put("id", base + "url")
                .put("label", base + "name");
        final JSONObject context = new JSONObject()
                .put("@context", device);
        // TODO ugly as hell
        // http://stackoverflow.com/questions/16563579/jsonobject-tostring-how-not-to-escape-slashes
        return Response.ok(context.toString().replace("\\","")).build();
    }
}
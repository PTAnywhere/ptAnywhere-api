package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import com.cisco.pt.ipc.enums.DeviceType;
import com.cisco.pt.ipc.sim.Device;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.server.mvc.Viewable;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    private static Log logger = LogFactory.getLog(ConsoleResource.class);

    @Context
    UriInfo uri;

    private boolean deviceHasCommandLine(String deviceId) {
        return new CommandLineGetter(deviceId).call();
    }

    private String getRelativeEndpointURL(String deviceId) throws UnsupportedEncodingException {
        return "../endpoint/devices/" + URLEncoder.encode(deviceId, "UTF-8") + "/console";
    }

    private String getWebSocketURL(String deviceId) throws UnsupportedEncodingException {
        final URI endpoint = this.uri.getBaseUri().resolve(getRelativeEndpointURL(deviceId));
        String ret = endpoint.toString().replace("http://", "ws://");
        if (endpoint.getAuthority().endsWith("forge.kmi.open.ac.uk")) {
            // To avoid Apache's reverse proxy (which creates problems with Websockets)
            ret = ret.replace("forge.kmi.open.ac.uk", "forge.kmi.open.ac.uk:8080");
        }
        return ret;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDevice(@PathParam("device") String deviceId) {
        if (deviceHasCommandLine(deviceId)) {
            try {
                final String wsu = getWebSocketURL(deviceId);
                final Map<String, Object> map = new HashMap<String, Object>();
                map.put("websocketURL", wsu);
                return Response.ok(new Viewable("/console.ftl", map)).
                        link(wsu, "endpoint").build();
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);  // UTF-8 should be always supported.
                return Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            return Response.serverError().entity("This device does not have command line.").build();
        }
    }
}
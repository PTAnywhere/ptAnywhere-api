package uk.ac.open.kmi.forge.webPacketTracer.widget;

import com.cisco.pt.ipc.sim.Device;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.webPacketTracer.api.websocket.ConsoleEndpoint;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.session.SessionManager;
import uk.ac.open.kmi.forge.webPacketTracer.session.SessionsManager;

import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


class CommandLineGetter extends PTCallable<Boolean> {
    final String dId;
    public CommandLineGetter(String sId, String dId) {
        super(new SessionManager(sId, SessionsManager.create()));
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

@Path("sessions/{session}/devices/{device}/console")
public class ConsoleResource extends CustomAbstractResource {

    private static Log logger = LogFactory.getLog(ConsoleResource.class);

    private boolean deviceHasCommandLine(String sessionId, String deviceId) {
        return new CommandLineGetter(sessionId, deviceId).call();
    }

    // To avoid pointing to Apache's reverse proxy in the URL (this creates problems with Websockets)
    private String fixPort(URI uri) {
        if (uri.getAuthority().endsWith("forge.kmi.open.ac.uk")) {
            return uri.toString().replace("forge.kmi.open.ac.uk", "forge.kmi.open.ac.uk:8080");
        }
        return uri.toString();
    }

    private String getPathToEndpoint(String sessionId, String deviceId) throws UnsupportedEncodingException {
        final ServerEndpoint annotation = ConsoleEndpoint.class.getAnnotation(ServerEndpoint.class);
        return annotation.value().
                replace("{session}", sessionId).
                replace("{device}", URLEncoder.encode(deviceId, "UTF-8")).
                substring(1);  // Remove first slash because it will already be included in the App root URL
    }

    private String getWebSocketURL(String sessionId, String deviceId) throws UnsupportedEncodingException {
        return fixPort(getAppRootURL()).replace("http://", "ws://") + getPathToEndpoint(sessionId, deviceId);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDevice(@PathParam("session") String sessionId,
                              @PathParam("device") String deviceId) {
        if (deviceHasCommandLine(sessionId, deviceId)) {
            try {
                final String wsu = getWebSocketURL(sessionId, deviceId);
                final Map<String, Object> map = new HashMap<String, Object>();
                map.put("websocketURL", wsu);
                return Response.ok(getPreFilled("/console.ftl", map)).
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
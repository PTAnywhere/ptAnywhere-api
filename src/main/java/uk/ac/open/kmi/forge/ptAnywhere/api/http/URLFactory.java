package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import uk.ac.open.kmi.forge.ptAnywhere.api.websocket.ConsoleEndpoint;
import javax.websocket.server.ServerEndpoint;
import java.net.URI;


// TODO read the following and refactor:
// https://jersey.java.net/documentation/latest/user-guide.html#d0e10659
public class URLFactory {

    static public final String SESSION_PATH = "sessions";
    static public final String SESSION_PARAM = "session";
    static public final String NETWORK_PATH = "network";
    static public final String LINKS_PATH = "links";
    static public final String LINK_PARAM = "link";
    static public final String CONTEXT_PATH = "contexts";
    static public final String CONTEXT_DEVICE_PATH = "device.jsonld";
    static public final String DEVICE_PATH = "devices";
    static public final String DEVICE_PARAM = "device";
    static public final String PORT_PATH = "ports";
    static public final String PORT_PARAM = "port";
    static public final String PORT_LINK_PATH = "link";

    final URI baseUri;
    final String sessionId;
    final String deviceId;

    public URLFactory(URI baseUri, String sessionId) {
        this(baseUri, sessionId, null);
    }

    URLFactory(URI baseUri, String sessionId, String deviceId) {
        this.baseUri = baseUri;
        this.sessionId = sessionId;
        this.deviceId = deviceId;
    }

    public String getSessionURL() {
        return Utils.getURIWithSlashRemovingQuery(this.baseUri) + SESSION_PATH + "/" + this.sessionId + "/";
    }

    public String getDevicesURL() {
        return getSessionURL() + DEVICE_PATH + "/";
    }

    public String createDeviceURL(String id) {
        return getDevicesURL() + id + "/";
    }

    // To avoid pointing to Apache's reverse proxy in the URL (this creates problems with Websockets)
    private String fixPort(URI uri) {
        if (uri.getAuthority().endsWith("forge.kmi.open.ac.uk")) {
            return uri.toString().replace("forge.kmi.open.ac.uk", "forge.kmi.open.ac.uk:8080");
        }
        return uri.toString();
    }

    private String getPathToEndpoint(String sessionId, String deviceId) {
        final ServerEndpoint annotation = ConsoleEndpoint.class.getAnnotation(ServerEndpoint.class);
        return annotation.value().
                replace("{session}", sessionId).
                replace("{device}", deviceId).
                substring(1);  // Remove first slash because it will already be included in the App root URL
    }

    public String createConsoleEndpoint(String deviceId) {
        return fixPort(this.baseUri.resolve("../")).replace("http://", "ws://") + getPathToEndpoint(sessionId, deviceId);
    }

    public String createLinkURL(String id) {
        return getSessionURL() + LINKS_PATH + "/" + id;
    }

    public String createPortsURL() {
        return createDeviceURL(this.deviceId) + PORT_PATH + "/";
    }

    public String createPortURL(String portId) {
        return createPortsURL() + Utils.escapePort(portId) + "/";
    }

    public String createPortURL(String deviceId, String portId) {
        return createDeviceURL(deviceId) + PORT_PATH + "/" + Utils.escapePort(portId) + "/";
    }

    public String createPortLinkURL(String portId) {
        return createPortURL(portId) + PORT_LINK_PATH;
    }

    protected static String extractElement(String url, String id) {
        boolean next = false;
        for(String piece: url.split("/")) {
            if (next) return piece;
            next = piece.equals(id);
        }
        return null;
    }

    public static String parseDeviceId(String url) {
        return extractElement(url, DEVICE_PATH);
    }

    public static String parsePortId(String url) {
        return Utils.unescapePort(extractElement(url, PORT_PATH));
    }
}

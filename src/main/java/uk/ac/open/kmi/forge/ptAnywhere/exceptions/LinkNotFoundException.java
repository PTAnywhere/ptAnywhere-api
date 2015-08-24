package uk.ac.open.kmi.forge.ptAnywhere.exceptions;

import javax.ws.rs.core.Response;


public class LinkNotFoundException extends PTAnywhereException {

    // BEGIN: used mainly for swagger doc.
    final public static int status = 404; // Response.Status.NOT_FOUND.getStatusCode() is not a constant for Java
    final public static String description = "Either the link, the port or the device does not exist.";
    final public static String description2 = "The link does not exist.";
    // END: used mainly for swagger doc.

    public LinkNotFoundException(String linkId) {
        super(Response.Status.NOT_FOUND,  "No link was found with id \"" + linkId + "\"");
    }

    public LinkNotFoundException(String deviceId, String portId) {
        super(Response.Status.NOT_FOUND,  "The port \"" + portId + "\" in device \"" + deviceId + "\" is disconnected.");
    }
}
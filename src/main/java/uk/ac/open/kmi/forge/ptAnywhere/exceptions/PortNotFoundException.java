package uk.ac.open.kmi.forge.ptAnywhere.exceptions;

import javax.ws.rs.core.Response;


public class PortNotFoundException extends PTAnywhereException {

    // BEGIN: used mainly for swagger doc.
    final public static int status = 404; // Response.Status.NOT_FOUND.getStatusCode() is not a constant for Java
    final public static String description = "No port exists with the given id.";
    // END: used mainly for swagger doc.

    public PortNotFoundException(String deviceId, String portId) {
        super(Response.Status.NOT_FOUND,  "No port was found with id \"" + portId + "\" in the device \"" + deviceId + "\"");
    }
}
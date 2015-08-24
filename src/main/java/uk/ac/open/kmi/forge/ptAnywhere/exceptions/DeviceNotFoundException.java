package uk.ac.open.kmi.forge.ptAnywhere.exceptions;

import javax.ws.rs.core.Response;


public class DeviceNotFoundException extends PTAnywhereException {

    // BEGIN: used mainly for swagger doc.
    final public static int status = 404; // Response.Status.NOT_FOUND.getStatusCode() is not a constant for Java
    final public static String description = "The device does not exist";
    // END: used mainly for swagger doc.

    public DeviceNotFoundException(String deviceId) {
        super(Response.Status.NOT_FOUND,  "No device was found with id \"" + deviceId + "\"");
    }
}
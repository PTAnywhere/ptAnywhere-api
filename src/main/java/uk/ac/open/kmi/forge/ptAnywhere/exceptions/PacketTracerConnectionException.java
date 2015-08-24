package uk.ac.open.kmi.forge.ptAnywhere.exceptions;

import javax.ws.rs.core.Response;


public class PacketTracerConnectionException extends PTAnywhereException {

    // BEGIN: used mainly for swagger doc.
    final public static int status = 503; // Response.Status.SERVICE_UNAVAILABLE.getStatusCode() is not a constant for Java
    final public static String description = "The application could not connect to the PT instance";
    // END: used mainly for swagger doc.

    public PacketTracerConnectionException() {
        this(PacketTracerConnectionException.description);
    }

    public PacketTracerConnectionException(String message) {
        super(Response.Status.SERVICE_UNAVAILABLE, message);
    }

    public PacketTracerConnectionException(String message, Throwable t) {
        super(Response.Status.SERVICE_UNAVAILABLE, message, t);
    }
}
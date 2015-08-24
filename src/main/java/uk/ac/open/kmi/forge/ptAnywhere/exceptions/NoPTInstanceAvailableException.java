package uk.ac.open.kmi.forge.ptAnywhere.exceptions;

import javax.ws.rs.core.Response;


public class NoPTInstanceAvailableException extends PTAnywhereException {

    // BEGIN: used mainly for swagger doc.
    final public static int status = 503; // Response.Status.SERVICE_UNAVAILABLE.getStatusCode() is not a constant for Java
    final public static String description = "Limit reached in PT instance creation";
    // END: used mainly for swagger doc.

    public NoPTInstanceAvailableException() {
        this(NoPTInstanceAvailableException.description + ". Please retry again.");
    }

    public NoPTInstanceAvailableException(String message) {
        super(Response.Status.SERVICE_UNAVAILABLE, message);
    }
}
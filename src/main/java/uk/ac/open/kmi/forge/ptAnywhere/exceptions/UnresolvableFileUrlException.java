package uk.ac.open.kmi.forge.ptAnywhere.exceptions;

import javax.ws.rs.core.Response;


public class UnresolvableFileUrlException extends PTAnywhereException {

    // BEGIN: used mainly for swagger doc.
    final public static int status = 400; // Response.Status.NOT_FOUND.getStatusCode() is not a constant for Java
    final public static String description = "The URL passed could not be resolved and therefore cannot be opened";
    // END: used mainly for swagger doc.

    public UnresolvableFileUrlException() {
        super(Response.Status.NOT_ACCEPTABLE,  description);
    }

    public UnresolvableFileUrlException(String fileUrl) {
        super(Response.Status.NOT_ACCEPTABLE,  "The following URL could not be resolved: \"" + fileUrl + "\"");
    }
}
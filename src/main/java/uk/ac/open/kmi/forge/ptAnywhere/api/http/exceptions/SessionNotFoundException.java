package uk.ac.open.kmi.forge.ptAnywhere.api.http.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;


// TODO implement ExceptionMapper?
// http://stackoverflow.com/questions/15185299/jax-rs-jersey-exceptionmappers-user-defined-exception
// FIXME store past sessions IDs to differentiate between 404 and 410 errors?
public class SessionNotFoundException extends WebApplicationException {

    // BEGIN: used mainly for swagger doc.
    final public static int status = 410; // Response.Status.GONE.getStatusCode() is not a constant for Java
    final public static String description = "No active session exists with the given id. " +
            "This session (and therefore your requested URL) might have existed in the past, but it does not anymore.";
    // END: used mainly for swagger doc.

    public SessionNotFoundException(String sessionId, Link... link) {
        super(Response.status(Response.Status.GONE).
                entity(new ErrorBean(
                        Response.Status.GONE.getStatusCode(),
                        "No session was found with id \"" + sessionId + "\"")).
                links(link).build()); // is it better to use GONE???
    }
}
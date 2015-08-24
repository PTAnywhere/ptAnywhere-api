package uk.ac.open.kmi.forge.ptAnywhere.exceptions;


import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;


// TODO implement ExceptionMapper?
// http://stackoverflow.com/questions/15185299/jax-rs-jersey-exceptionmappers-user-defined-exception
// FIXME store past sessions IDs to differentiate between 404 and 410 errors?
public class SessionNotFoundException extends PTAnywhereException {

    // BEGIN: used mainly for swagger doc.
    final public static int status = 410; // Response.Status.GONE.getStatusCode() is not a constant for Java
    final public static String description = "No active session exists with the given id. " +
            "This session (and therefore your requested URL) might have existed in the past, but it does not anymore.";
    // END: used mainly for swagger doc.

    public SessionNotFoundException(String sessionId, Link... links) {
        // Is it better to use NOT_FOUND???
        super(Response.Status.GONE, "No session was found with id \"" + sessionId + "\"", links);
    }
}
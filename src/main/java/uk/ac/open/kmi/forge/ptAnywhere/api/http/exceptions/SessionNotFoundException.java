package uk.ac.open.kmi.forge.ptAnywhere.api.http.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;


// TODO implement ExceptionMapper?
// http://stackoverflow.com/questions/15185299/jax-rs-jersey-exceptionmappers-user-defined-exception
public class SessionNotFoundException extends WebApplicationException {
    public SessionNotFoundException(String sessionId, Link... link) {
        super(Response.status(Response.Status.NOT_FOUND).
                entity(new ErrorBean(
                        Response.Status.NOT_FOUND.getStatusCode(),
                        "No session was found with id \"" + sessionId + "\"")).
                links(link).build()); // is it better to use GONE???
    }
}
package uk.ac.open.kmi.forge.ptAnywhere.api.http.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


public class PacketTracerConnectionException extends WebApplicationException {

    public PacketTracerConnectionException() {
        this("The application could not connect to the PT instance. Please retry again.");
    }

    public PacketTracerConnectionException(String message) {
        super(Response.status(Response.Status.SERVICE_UNAVAILABLE).
                entity(new ErrorBean(
                        Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), message)).build());

    }

    public PacketTracerConnectionException(String message, Throwable t) {
        super(t, Response.status(Response.Status.SERVICE_UNAVAILABLE).
                entity(new ErrorBean(
                        Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), message)).build());

    }
}
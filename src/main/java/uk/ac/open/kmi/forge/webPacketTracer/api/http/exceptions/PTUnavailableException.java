package uk.ac.open.kmi.forge.webPacketTracer.api.http.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


public class PTUnavailableException extends WebApplicationException {
    public PTUnavailableException() {
        this("The PT instance could not be connected. Please retry.");
    }

    public PTUnavailableException(String message) {
        super(Response.status(Response.Status.SERVICE_UNAVAILABLE).
                entity(new ErrorBean(
                        Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), message)).build());
    }
}
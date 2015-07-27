package uk.ac.open.kmi.forge.webPacketTracer.api.http.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


public class NoPTInstanceAvailableException extends WebApplicationException {
    public NoPTInstanceAvailableException() {
        this("Limit reached in PT instance creation. Please wait before trying it again.");
    }

    public NoPTInstanceAvailableException(String message) {
        super(Response.status(Response.Status.SERVICE_UNAVAILABLE).
                entity(new ErrorBean(
                        Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), message)).build());
    }
}
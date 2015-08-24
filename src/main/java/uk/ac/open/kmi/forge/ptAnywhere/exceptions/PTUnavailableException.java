package uk.ac.open.kmi.forge.ptAnywhere.exceptions;

import javax.ws.rs.core.Response;


public class PTUnavailableException extends PTAnywhereException {
    public PTUnavailableException() {
        this("The PT instance could not be connected. Please retry.");
    }

    public PTUnavailableException(String message) {
        super(Response.Status.SERVICE_UNAVAILABLE, message);
    }
}
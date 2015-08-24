package uk.ac.open.kmi.forge.ptAnywhere.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;


public abstract class PTAnywhereException extends WebApplicationException {
    public PTAnywhereException(Response.Status status, String message, Link... links) {
        super(ErrorBean.createError(status, message).links(links).build());
    }
    public PTAnywhereException(Response.Status status, String message, Throwable t, Link... links) {
        super(t, ErrorBean.createError(status, message).links(links).build());
    }
}
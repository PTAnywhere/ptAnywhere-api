package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;


public class SessionNotFoundException extends WebApplicationException {
    public SessionNotFoundException(String sessionId, Link... link) {
        super(Response.status(Response.Status.NOT_FOUND).
                entity(new ErrorBean(
                        Response.Status.NOT_FOUND.getStatusCode(),
                        "No session was found with the id " + sessionId)).
                links(link).build()); // is it better to use GONE???
    }
}


class ErrorBean {
    private String errorMsg;
    private int errorCode;

    public ErrorBean() {}

    public ErrorBean(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
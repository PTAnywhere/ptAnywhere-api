package uk.ac.open.kmi.forge.ptAnywhere.api.http.exceptions;

import javax.ws.rs.core.Response;

public class ErrorBean {
    private String errorMsg;
    private int errorCode;

    public ErrorBean() {}

    public ErrorBean(Response.Status statusCode, String errorMsg) {
        this(statusCode.getStatusCode(), errorMsg);
    }

    public ErrorBean(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public static Response createError(Response.Status status, String message) {
        return Response.status(status).
                entity(new ErrorBean(status.getStatusCode(), message)).build();
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

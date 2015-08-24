package uk.ac.open.kmi.forge.ptAnywhere.exceptions;

import javax.ws.rs.core.Response;

public class ErrorBean {
    private String errorMsg;
    private int errorCode;

    public ErrorBean() {}

    private ErrorBean(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public static Response.ResponseBuilder createError(Response.Status status, String message) {
        return Response.status(status).
                entity(new ErrorBean(status.getStatusCode(), message));
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

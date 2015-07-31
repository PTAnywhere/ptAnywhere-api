package uk.ac.open.kmi.forge.ptAnywhere.api.http.exceptions;

public class ErrorBean {
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

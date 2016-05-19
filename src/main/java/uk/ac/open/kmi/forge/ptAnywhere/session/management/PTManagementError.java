package uk.ac.open.kmi.forge.ptAnywhere.session.management;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class PTManagementError {
    int status;
    String message;

    public PTManagementError() {
    }

    public PTManagementError(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
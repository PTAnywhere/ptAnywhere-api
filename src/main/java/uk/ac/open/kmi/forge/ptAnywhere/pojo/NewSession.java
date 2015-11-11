package uk.ac.open.kmi.forge.ptAnywhere.pojo;


public class NewSession {

    String fileUrl;

    public NewSession() {}

    public NewSession(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}

package uk.ac.open.kmi.forge.ptAnywhere.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(value="NewSession", description="Specifications of the new session to be created.")
public class NewSession {

    String fileUrl;
    String sessionId;

    public NewSession() {}

    public NewSession(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @ApiModelProperty(value="URL of a Packet Tracer file to be opened in the new session.", required=true)
    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @ApiModelProperty(value="Identifies the user of the new session as the same as the session ID provided.")
    public String getSameUserAsInSession() {
        return sessionId;
    }

    public void setSameUserAsInSession(String sessionId) {
        this.sessionId = sessionId;
    }
}

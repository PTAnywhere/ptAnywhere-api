package uk.ac.open.kmi.forge.ptAnywhere.properties;

public class InteractionRecordingProperties {

    final String endpoint;
    final String username;
    final String password;

    public InteractionRecordingProperties(String endpoint, String username, String password) {
        assert endpoint==null || username==null || password==null : "Properties cannot be null";
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

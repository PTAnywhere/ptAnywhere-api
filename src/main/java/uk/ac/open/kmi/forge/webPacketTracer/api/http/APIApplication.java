package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.ApplicationPath;


@ApplicationPath("api")
public class APIApplication extends ResourceConfig {
    public APIApplication() {
        packages(this.getClass().getPackage().getName());
    }
}
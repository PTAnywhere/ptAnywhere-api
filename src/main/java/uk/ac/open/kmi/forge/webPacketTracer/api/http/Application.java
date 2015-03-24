package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;


// TODO I would like to get rid of this, but I have not found other way to register Freemarker.
// https://jersey.java.net/documentation/latest/mvc.html#mvc.registration
// https://github.com/jersey/jersey/blob/master/examples/freemarker-webapp/
public class Application extends ResourceConfig {
    public Application() {
        super(ConsoleResource.class, DeviceResource.class,
                DevicesResource.class, LinkResource.class, NetworkResource.class,
                PortLinkResource.class, PortResource.class, PortsResource.class);
        register(FreemarkerMvcFeature.class);
    }
}
package uk.ac.open.kmi.forge.ptAnywhere.management;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;

import javax.ws.rs.ApplicationPath;


// TODO I would like to get rid of this, but I have not found other way to register Freemarker.
// https://jersey.java.net/documentation/latest/mvc.html#mvc.registration
// https://github.com/jersey/jersey/blob/master/examples/freemarker-webapp/
@ApplicationPath("management")
public class ManagementApplication extends ResourceConfig {
    public ManagementApplication() {
        super(new ResourceConfig().
                        register(FreemarkerMvcFeature.class).
                        packages(ManagementApplication.class.getPackage().getName()).
                        property(MvcFeature.TEMPLATE_BASE_PATH, "templates").
                        property(FreemarkerMvcFeature.CACHE_TEMPLATES, true)
        );
    }
}
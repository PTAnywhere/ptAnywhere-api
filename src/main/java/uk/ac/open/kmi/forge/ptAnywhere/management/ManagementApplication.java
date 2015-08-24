package uk.ac.open.kmi.forge.ptAnywhere.management;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PropertyFileManager;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;


// TODO I would like to get rid of this, but I have not found other way to register Freemarker.
// https://jersey.java.net/documentation/latest/mvc.html#mvc.registration
// https://github.com/jersey/jersey/blob/master/examples/freemarker-webapp/
@ApplicationPath("management")
public class ManagementApplication extends ResourceConfig {

    public static final String APP_ROOT = "path";

    public ManagementApplication(@Context ServletContext servletContext) {
        super(new ResourceConfig().
                        register(FreemarkerMvcFeature.class).
                        packages(ManagementApplication.class.getPackage().getName()).
                        property(MvcFeature.TEMPLATE_BASE_PATH, "templates").
                        property(FreemarkerMvcFeature.CACHE_TEMPLATES, true)
        );
        final PropertyFileManager pfm = new PropertyFileManager();
        servletContext.setAttribute(APP_ROOT, pfm.getApplicationPath());
    }
}
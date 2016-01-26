package uk.ac.open.kmi.forge.ptAnywhere.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.server.mvc.Viewable;
import uk.ac.open.kmi.forge.ptAnywhere.ContextListener;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManager;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManagerFactory;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public abstract class CustomAbstractResource {

    private static Log logger;
    private static Properties properties;

    static String RELATIVE_ROOT_PATH = "../";

    static {
        logger = LogFactory.getLog(CustomAbstractResource.class);
        properties = new Properties();  // It does not change once the app has been deployed.
    }

    @Context
    ServletContext servletContext;

    protected String getApplicationTitle() {
        try {
            properties.load(CustomAbstractResource.class.getClassLoader().getResourceAsStream("environment.properties"));
        } catch (IOException e) {
            logger.error("Host and port of the PT instance could not be read from the properties file, using default values.");
        } finally {
            return properties.getProperty("title", "PacketTracer Widget");
        }
    }

    String getAppRootURL() {
        return (String) this.servletContext.getAttribute(ContextListener.APPLICATION_ROOT);
    }

    SessionsManager getSessionsManager() {
        return ((SessionsManagerFactory) this.servletContext.getAttribute(ContextListener.SESSIONS_MANAGER_FACTORY)).create();
    }

    String getAPIURL() {
        return getAppRootURL().toString() + "v1/";
    }

    public Viewable getPreFilled(String path) {
        return getPreFilled(path, new HashMap<String, Object>());
    }

    public Viewable getPreFilled(String path, Map<String, Object> map) {
        map.put("base", getAppRootURL().toString() + "static/");
        map.put("api", getAPIURL());
        return (new Viewable(path, map));
    }
}
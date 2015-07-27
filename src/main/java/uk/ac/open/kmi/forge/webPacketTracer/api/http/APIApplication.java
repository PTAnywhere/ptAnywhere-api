package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.server.ResourceConfig;
import uk.ac.open.kmi.forge.webPacketTracer.analytics.InteractionRecord;
import uk.ac.open.kmi.forge.webPacketTracer.analytics.InteractionRecordFactory;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


@ApplicationPath("api")
public class APIApplication extends ResourceConfig {

    private static final String INTERACTION_RECORD_FACTORY = "interactionRecordFactory";
    private static final Log LOGGER = LogFactory.getLog(APIApplication.class);

    // Executor used by the application for:
    //   1. Ensuring that the TinCanAPI clients execute in their own threads.
    //   2. Have a blocking Redis subscription object running during the application lifetime.
    private final ExecutorService executor;

    // FIXME not sure whether creating one PropertyFileManager per request could be harmful.
    // Just in case, only an object is created per application so we are not reading the file over and over again.
    private final InteractionRecordFactory irf;


    public APIApplication(@Context ServletContext servletContext) {
        LOGGER.debug("Creating API webapp.");
        packages(getClass().getPackage().getName());

        this.executor = Executors.newFixedThreadPool(10, new SimpleDaemonFactory());
        this.irf = new InteractionRecordFactory(this.executor);
        servletContext.setAttribute(INTERACTION_RECORD_FACTORY, this.irf);
    }

    public static InteractionRecord createInteractionRecord(ServletContext servletContext) {
        return ((InteractionRecordFactory) servletContext.getAttribute(APIApplication.INTERACTION_RECORD_FACTORY)).create();
    }

    //@PostConstruct
    @PreDestroy
    public void stop() {
        LOGGER.debug("Destroying API webapp.");
        this.executor.shutdownNow();
    }
}

// Following the advice from:
//   http://stackoverflow.com/questions/3745905/what-is-recommended-way-for-spawning-threads-from-a-servlet-in-tomcat
class SimpleDaemonFactory implements ThreadFactory {
    public Thread newThread(Runnable r) {
        final Thread t =new Thread(r);
        t.setDaemon(true);
        return t;
    }
}


/*@Provider
class JsonMoxyConfigurationContextResolver implements ContextResolver<MoxyJsonConfig> {
    private final MoxyJsonConfig config;

    public JsonMoxyConfigurationContextResolver() {
        config = new MoxyJsonConfig();
                .setAttributePrefix("@") // Needed also for @ at root element :-S
                .setIncludeRoot(true);
    }

    @Override
    public MoxyJsonConfig getContext(Class<?> type) {
        return config;
    }
}*/
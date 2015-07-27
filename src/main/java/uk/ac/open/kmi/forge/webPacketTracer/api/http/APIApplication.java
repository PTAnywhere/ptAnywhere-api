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


@ApplicationPath("api")
public class APIApplication extends ResourceConfig {

    private static final String INTERACTION_RECORD_FACTORY = "interactionRecordFactory";
    private static final Log LOGGER = LogFactory.getLog(APIApplication.class);

    private final InteractionRecordFactory irf;

    public APIApplication(@Context ServletContext servletContext) {
        LOGGER.debug("Creating API webapp.");
        packages(getClass().getPackage().getName());
        this.irf = new InteractionRecordFactory();
        servletContext.setAttribute(INTERACTION_RECORD_FACTORY, this.irf);
    }

    public static InteractionRecord createInteractionRecord(ServletContext servletContext) {
        return ((InteractionRecordFactory) servletContext.getAttribute(APIApplication.INTERACTION_RECORD_FACTORY)).create();
    }

    //@PostConstruct
    @PreDestroy
    public void stop() {
        LOGGER.debug("Destroying API webapp.");
        this.irf.shutdown();
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
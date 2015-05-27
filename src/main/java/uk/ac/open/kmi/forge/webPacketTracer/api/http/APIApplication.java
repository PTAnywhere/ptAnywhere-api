package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.ApplicationPath;


@ApplicationPath("api")
public class APIApplication extends ResourceConfig {
    public APIApplication() {
        packages(getClass().getPackage().getName());
        //register(JsonMoxyConfigurationContextResolver.class);
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
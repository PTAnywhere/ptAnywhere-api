package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import io.swagger.config.ScannerFactory;
import io.swagger.jaxrs.config.ReflectiveJaxrsScanner;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.server.ResourceConfig;
import uk.ac.open.kmi.forge.ptAnywhere.ContextListener;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecord;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecordFactory;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.filters.CORSFilter;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PropertyFileManager;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManager;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManagerFactory;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;


@ApplicationPath("v1")
public class APIApplication extends ResourceConfig {

    private static final Log LOGGER = LogFactory.getLog(APIApplication.class);


    public APIApplication(@Context ServletContext servletContext) throws InterruptedException {
        ContextListener.initSignal.await();

        LOGGER.info("Creating API webapp.");

        final PropertyFileManager pfm = getPropertyFile(servletContext);
        packages(false, getClass().getPackage().getName());  // Not recursive
        if (pfm.doesAPIAllowCORS()) {
            register(CORSFilter.class);
        }
        configSwagger(servletContext, pfm.getApplicationPath());
    }

    protected void configSwagger(ServletContext context, String appPath) {
        register(ApiListingResource.class);
        register(SwaggerSerializers.class);

        final ReflectiveJaxrsScanner scanner = new ReflectiveJaxrsScanner();
        scanner.setResourcePackage(getClass().getPackage().getName());
        ScannerFactory.setScanner(scanner);

        // It would be clearer to use the SwaggerDefinition annotation, but I does not seem to work
        // and I cannot find a working example in the official documentation.
        final Info info = new Info()
                .title("PTAnywhere API")
                .description("API for consuming PTAnywhere.")
                .version("1.0.1")
                //.termsOfService("http://swagger.io/terms/")
                .contact(new Contact()
                        .name("Aitor Gomez-Goiri")
                        .email("aitor.gomez-goiri@open.ac.uk"));/*
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));*/
        final Swagger swagger = new Swagger().info(info).basePath(appPath + "v1");
        swagger.tag(new Tag()
                .name("session")
                .description("Operations to manage sessions"));
        swagger.tag(new Tag()
                .name("network")
                .description("Network topology related operations"));
        swagger.tag(new Tag()
                .name("device")
                .description("Device-centric operations"));
        context.setAttribute("swagger", swagger);
    }

    private PropertyFileManager getPropertyFile(ServletContext servletContext) {
        return ((PropertyFileManager) servletContext.getAttribute(ContextListener.PROPERTIES));
    }

    private static String getReferrerWidgetURL(HttpServletRequest request) {
        final String referrer = request.getHeader("referer");
        if (referrer==null) return null;
        final int paramsAt = referrer.indexOf("?");
        if (paramsAt==-1) return referrer;
        return referrer.substring(0, paramsAt);
    }

    private static InteractionRecordFactory getRecordFactory(ServletContext servletContext) {
        return ((InteractionRecordFactory) servletContext.getAttribute(ContextListener.INTERACTION_RECORD_FACTORY));
    }

    public static InteractionRecord createInteractionRecordForNewSession(ServletContext servletContext, HttpServletRequest request, String sessionId, String oldSessionId) {
        return getRecordFactory(servletContext).createForNewSession(getReferrerWidgetURL(request), sessionId, oldSessionId);
    }

    public static InteractionRecord createInteractionRecord(ServletContext servletContext, HttpServletRequest request, String sessionId) {
        return getRecordFactory(servletContext).create(getReferrerWidgetURL(request), sessionId);
    }

    protected static SessionsManagerFactory getSessionsManagerFactory(ServletContext servletContext) {
        return (SessionsManagerFactory) servletContext.getAttribute(ContextListener.SESSIONS_MANAGER_FACTORY);
    }

    public static SessionsManager createSessionsManager(ServletContext servletContext) {
        return getSessionsManagerFactory(servletContext).create();
    }

    //@PostConstruct
    @PreDestroy
    public void stop() {
        LOGGER.info("Destroying API webapp.");
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
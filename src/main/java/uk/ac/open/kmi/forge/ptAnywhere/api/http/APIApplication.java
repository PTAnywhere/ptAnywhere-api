package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import io.swagger.config.ScannerFactory;
import io.swagger.jaxrs.config.ReflectiveJaxrsScanner;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.server.ResourceConfig;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecord;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecordFactory;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.filters.CORSFilter;
import uk.ac.open.kmi.forge.ptAnywhere.api.websocket.ConsoleEndpoint;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PropertyFileManager;
import uk.ac.open.kmi.forge.ptAnywhere.session.ExpirationSubscriber;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManager;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManagerFactory;
import uk.ac.open.kmi.forge.ptAnywhere.session.impl.SessionsManagerFactoryImpl;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


@ApplicationPath("v1")
public class APIApplication extends ResourceConfig {

    private static final String SESSIONS_MANAGER_FACTORY = "sessionsManagerFactory";
    private static final String INTERACTION_RECORD_FACTORY = "interactionRecordFactory";
    private static final Log LOGGER = LogFactory.getLog(APIApplication.class);

    // Executor used by the application for:
    //   1. Ensuring that the TinCanAPI clients execute in their own threads.
    //   2. Have a blocking Redis subscription object running during the application lifetime.
    private final ExecutorService executor;

    private final InteractionRecordFactory irf;
    private final ExpirationSubscriber es;


    public APIApplication(@Context ServletContext servletContext) {
        LOGGER.info("Creating API webapp.");

        // Only an object is created per application, so we are not reading the file over and over again.
        // However, I'm not sure whether creating one PropertyFileManager per request could be harmful or desirable.
        final PropertyFileManager pfm = new PropertyFileManager();

        packages(false, getClass().getPackage().getName());  // Not recursive
        if (pfm.doesAPIAllowCORS()) {
            register(CORSFilter.class);
        }
        configSwagger(servletContext, pfm.getApplicationPath());

        final SessionsManagerFactory sessionsManagerFactory = SessionsManagerFactoryImpl.create(pfm);
        servletContext.setAttribute(SESSIONS_MANAGER_FACTORY, sessionsManagerFactory);
        ConsoleEndpoint.setSessionsManagerFactory(sessionsManagerFactory);
        this.es =  sessionsManagerFactory.createExpirationSubscription();  // WARNING: it can return null.

        this.executor = Executors.newFixedThreadPool(20, new SimpleDaemonFactory());
        this.irf = new InteractionRecordFactory(this.executor, pfm.getInteractionRecordingDetails());
        ConsoleEndpoint.setInteractionRecordFactory(this.irf);
        servletContext.setAttribute(INTERACTION_RECORD_FACTORY, this.irf);

        if (this.es!=null) {
            // WARNING: Blocking thread. It won't stop during the Application lifecycle.
            this.executor.submit(this.es);
        }
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

    private static String getReferrerWidgetURL(HttpServletRequest request) {
        final String referrer = request.getHeader("referer");
        if (referrer==null) return null;
        final int paramsAt = referrer.indexOf("?");
        if (paramsAt==-1) return referrer;
        return referrer.substring(0, paramsAt);
    }

    private static InteractionRecordFactory getRecordFactory(ServletContext servletContext) {
        return ((InteractionRecordFactory) servletContext.getAttribute(APIApplication.INTERACTION_RECORD_FACTORY));
    }

    public static InteractionRecord createInteractionRecord(ServletContext servletContext, HttpServletRequest request, String sessionId) {
        return getRecordFactory(servletContext).create(getReferrerWidgetURL(request), sessionId);
    }

    public static SessionsManager createSessionsManager(ServletContext servletContext) {
        return ((SessionsManagerFactory) servletContext.getAttribute(APIApplication.SESSIONS_MANAGER_FACTORY)).create();
    }

    //@PostConstruct
    @PreDestroy
    public void stop() {
        LOGGER.info("Destroying API webapp.");
        if (this.es!=null) {
            this.es.stop();  // Destroying the Executor would do the work too, but just in case.
        }
        this.executor.shutdownNow();
    }
}

// Following the advice from:
//   http://stackoverflow.com/questions/3745905/what-is-recommended-way-for-spawning-threads-from-a-servlet-in-tomcat
class SimpleDaemonFactory implements ThreadFactory {
    public Thread newThread(Runnable r) {
        final Thread t =new Thread(r);
        t.setDaemon(true);
        t.setUncaughtExceptionHandler(new UEHLogger());
        return t;
    }
}

class UEHLogger implements Thread.UncaughtExceptionHandler {
    private static final Log LOGGER = LogFactory.getLog(UEHLogger.class);
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.fatal("Thread terminated with exception: " + t.getName(), e);
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
package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import io.swagger.config.ScannerFactory;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.ReflectiveJaxrsScanner;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Swagger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.server.ResourceConfig;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecord;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecordFactory;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PropertyFileManager;
import uk.ac.open.kmi.forge.ptAnywhere.session.ExpirationSubscriber;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManager;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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

    // FIXME not sure whether creating one PropertyFileManager per request could be harmful or desirable.
    // Just in case, only an object is created per application so we are not reading the file over and over again.
    private final InteractionRecordFactory irf;
    private final ExpirationSubscriber es;


    public APIApplication(@Context ServletContext servletContext) {
        LOGGER.debug("Creating API webapp.");

        // Only an object is created per application, so we are not reading the file over and over again.
        final PropertyFileManager pfm = new PropertyFileManager();

        packages(getClass().getPackage().getName());
        configSwagger(servletContext, pfm.getApplicationPath());

        this.executor = Executors.newFixedThreadPool(20, new SimpleDaemonFactory());
        this.irf = new InteractionRecordFactory(this.executor, pfm.getInteractionRecordingDetails());
        this.es =  SessionsManager.createExpirationSubscription();
        servletContext.setAttribute(INTERACTION_RECORD_FACTORY, this.irf);

        // WARNING: Blocking thread. It won't stop during the Application lifecycle.
        this.executor.submit(this.es);
    }

    protected void configSwagger(ServletContext context, String appPath) {
        register(ApiListingResource.class);
        register(SwaggerSerializers.class);

        final ReflectiveJaxrsScanner scanner = new ReflectiveJaxrsScanner();
        scanner.setResourcePackage(getClass().getPackage().getName());
        ScannerFactory.setScanner(scanner);

        final Info info = new Info()
                .title("PTAnywhere API")
                .description("API for consuming PTAnywhere.")
                .version("1.0.1")
                .termsOfService("http://swagger.io/terms/")
                .contact(new Contact()
                        .name("Aitor")
                        .email("aitor.gomez-goiri@open.ac.uk"));/*
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));*/
        final Swagger swagger = new Swagger().info(info).basePath(appPath + "api");
        context.setAttribute("swagger", swagger);
    }

    public static InteractionRecord createInteractionRecord(ServletContext servletContext) {
        return ((InteractionRecordFactory) servletContext.getAttribute(APIApplication.INTERACTION_RECORD_FACTORY)).create();
    }

    //@PostConstruct
    @PreDestroy
    public void stop() {
        LOGGER.debug("Destroying API webapp.");
        this.es.stop();  // Destroying the Executor would do the work too, but just in case.
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
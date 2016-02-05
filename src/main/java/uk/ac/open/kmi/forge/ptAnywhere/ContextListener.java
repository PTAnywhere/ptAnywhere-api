package uk.ac.open.kmi.forge.ptAnywhere;

import com.rusticisoftware.tincan.RemoteLRS;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.InteractionRecordFactory;
import uk.ac.open.kmi.forge.ptAnywhere.api.websocket.ConsoleEndpoint;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.IdentityFinder;
import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.IdentityFinderFactory;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PacketTracerInstanceProperties;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PropertyFileManager;
import uk.ac.open.kmi.forge.ptAnywhere.session.ExpirationSubscriber;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManager;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManagerFactory;
import uk.ac.open.kmi.forge.ptAnywhere.session.impl.MultipleSessionsManagerFactory;
import uk.ac.open.kmi.forge.ptAnywhere.session.impl.SharedSessionsManagerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Set;
import java.util.concurrent.CountDownLatch;


/**
 * Some properties are shared at application level, i.e., two or more of the following classes use them:
 *      - APIApplication (web application)
 *      - ManagementAplication (web application)
 *      - ConsoleEndpoint (websocket endpoint)
 *
 * Furthermore, some resources need to be initialized before they are even instantiated (e.g., the Redis DB).
 *
 * This class will handle the creation and destroy of these shared resources.
 */
@WebListener
public class ContextListener implements ServletContextListener {

    /**
     * Signal which lets Application objects know when the context has been initialized.
     */
    // http://www.deadcoderising.com/execute-code-on-webapp-startup-and-shutdown-using-servletcontextlistener/
    public static final CountDownLatch initSignal = new CountDownLatch(1);

    public static final String PROPERTIES = "properties";
    public static final String SESSIONS_MANAGER_FACTORY = "sessionsManagerFactory";
    public static final String INTERACTION_RECORD_FACTORY = "interactionRecordFactory";
    public static final String APPLICATION_ROOT = "path";

    public static final Log LOGGER = LogFactory.getLog(ContextListener.class);

    private PoolManager pools;
    private ExpirationSubscriber es;


    private SessionsManagerFactory getSessionsManagerFactory(ServletContext context) {
        return (SessionsManagerFactory) context.getAttribute(SESSIONS_MANAGER_FACTORY);
    }

    private InteractionRecordFactory getInteractionRecordFactory(ServletContext context) {
        return (InteractionRecordFactory) context.getAttribute(INTERACTION_RECORD_FACTORY);
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        LOGGER.info("Creating context." + this);

        // Only an object is created per application, so we are not reading the file over and over again.
        // However, I'm not sure whether creating one PropertyFileManager per request could be harmful or desirable.
        final PropertyFileManager pfm = new PropertyFileManager();
        event.getServletContext().setAttribute(PROPERTIES, pfm);
        event.getServletContext().setAttribute(APPLICATION_ROOT, pfm.getApplicationPath());

        this.pools = new PoolManager(pfm);

        configureSessionManager(pfm, event.getServletContext());
        configureInteractionRecord(pfm, event.getServletContext());
        configureConsoleEndpoint(event.getServletContext());

        ContextListener.initSignal.countDown();
    }

    /**
     * Initializes Redis DB.
     */
    private void initRedis(SessionsManager session, Set<String> apis) {
        //session.clear();
        session.addManagementAPIs(apis.toArray(new String[apis.size()]));
    }

    private SessionsManagerFactory createSessionManagerFactory(PropertyFileManager properties) {
        final PacketTracerInstanceProperties sharedInstance = properties.getSharedInstanceDetails();
        if (sharedInstance==null) {
            return new MultipleSessionsManagerFactory(properties.getSessionHandlingDetails(),
                                                        this.pools,  // Needs to be initialized before calling this method
                                                        properties.getMaximumSessionLength());
        } else {
            return new SharedSessionsManagerFactory(sharedInstance);
        }
    }

    private void configureSessionManager(PropertyFileManager pfm, ServletContext context) {
        LOGGER.info("Creating factory for session management.");
        final SessionsManagerFactory factory = createSessionManagerFactory(pfm);

        this.es = factory.createExpirationSubscription();  // WARNING: it can return null.
        if (this.es!=null) {
            // WARNING: Blocking thread. It won't stop during the Application lifecycle.
            this.pools.getGeneralExecutor().submit(this.es);
        }

        initRedis(factory.create(), pfm.getPacketTracerManagementAPIs());

        context.setAttribute(SESSIONS_MANAGER_FACTORY, factory);
    }

    private void configureInteractionRecord(PropertyFileManager pfm, ServletContext context) {
        LOGGER.info("Creating factory for interaction record.");
        final IdentityFinder finder = IdentityFinderFactory.createHistoricalAnonymous(pfm.getInteractionRecordingDetails(),
                                                                                        this.pools.getCachePool());
        final InteractionRecordFactory irf = new InteractionRecordFactory(this.pools.getGeneralExecutor(),
                                                                            pfm.getInteractionRecordingDetails(),
                                                                            finder);
        context.setAttribute(INTERACTION_RECORD_FACTORY, irf);
    }

    private void configureConsoleEndpoint(ServletContext context) {
        ConsoleEndpoint.setSessionsManagerFactory( getSessionsManagerFactory(context) );
        ConsoleEndpoint.setInteractionRecordFactory( getInteractionRecordFactory(context) );
    }


    @Override
    public void contextDestroyed(ServletContextEvent event)  {
        LOGGER.info("Destroying context " + this);

        if (this.es!=null) {
            this.es.stop();  // Destroying the Executor would do the work too, but just in case.
        }
        getSessionsManagerFactory(event.getServletContext()).destroy();
        // Needed for InteractionRecordFactory!
        try {
            RemoteLRS.destroy();
        } catch(Exception e) {
            LOGGER.error("The RemoteLRS was not properly destroyed.");
            LOGGER.error(e.getMessage());
        }
        this.pools.close();
    }
}

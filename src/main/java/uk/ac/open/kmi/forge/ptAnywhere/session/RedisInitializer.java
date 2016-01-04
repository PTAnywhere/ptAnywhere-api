package uk.ac.open.kmi.forge.ptAnywhere.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PropertyFileManager;
import uk.ac.open.kmi.forge.ptAnywhere.session.impl.SessionsManagerFactoryImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Set;


/**
 * This class should configure backend PacketTracer instances in the Redis server.
 */
@WebListener
public class RedisInitializer implements ServletContextListener {

    private static final Log LOGGER = LogFactory.getLog(RedisInitializer.class);

    public void contextInitialized(ServletContextEvent event) {
        final PropertyFileManager pfm = new PropertyFileManager();
        final Set<String> apis = pfm.getPacketTracerManagementAPIs();
        final SessionsManagerFactory smf = SessionsManagerFactoryImpl.create(pfm);
        final SessionsManager session = smf.create();
        session.clear();
        session.addManagementAPIs(apis.toArray(new String[apis.size()]));
        smf.destroy();
    }

    public void contextDestroyed(ServletContextEvent event)  {
        //LOGGER.debug("destroying context");
    }
}

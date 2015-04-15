package uk.ac.open.kmi.forge.webPacketTracer.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * This class should configure backend PacketTracer instances in the Redis server.
 */
@WebListener
public class RedisInitializer implements ServletContextListener {

    private static final Log LOGGER = LogFactory.getLog(RedisInitializer.class);

    public void contextInitialized(ServletContextEvent arg0) {
        try {
            final SessionManager session = SessionManager.createSessionManager();
        } catch(Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void contextDestroyed(ServletContextEvent arg0)  {
        //LOGGER.debug("destroying context");
    }
}

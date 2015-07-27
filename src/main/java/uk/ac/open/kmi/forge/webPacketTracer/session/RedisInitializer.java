package uk.ac.open.kmi.forge.webPacketTracer.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.exceptions.JedisConnectionException;
import uk.ac.open.kmi.forge.webPacketTracer.properties.PropertyFileManager;

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

    public void contextInitialized(ServletContextEvent arg0) {
        try {
            final PropertyFileManager pfm = new PropertyFileManager();
            final Set<String> apis = pfm.getPacketTracerManagementAPIs();
            final SessionsManager session = SessionsManager.create();
            session.clear();
            session.addManagementAPIs(apis.toArray(new String[apis.size()]));
        } catch(Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void contextDestroyed(ServletContextEvent arg0)  {
        //LOGGER.debug("destroying context");
    }
}

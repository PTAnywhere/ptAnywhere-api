package uk.ac.open.kmi.forge.ptAnywhere.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


public class PropertyFileManager {

    private static final Log LOGGER = LogFactory.getLog(PropertyFileManager.class);

    public static final String defaultAPI = "http://localhost/";
    public static final String defaultHostname = "localhost";

    private final Properties props = new Properties();

    public PropertyFileManager() {
        try {
            this.props.load(PropertyFileManager.class.getClassLoader().getResourceAsStream("environment.properties"));
        } catch(IOException e) {
            LOGGER.error("The properties file could not be read.");
        }
    }

    /**
     * @return
     *  Application path under the webserver.
     *  For example: "/" or "context1/"
     */
    public  String getApplicationPath() {
        final String prop = this.props.getProperty("tomcat.path", "/");
        if (prop.endsWith("/")) return prop;
        return prop + "/";
    }

    /**
     * By default, the Redis server runs in the same machine as the web server.
     */
    public  RedisConnectionProperties getRedisConnectionDetails() {
        return new RedisConnectionProperties(
                this.props.getProperty("redis-host", PropertyFileManager.defaultHostname),
                Integer.parseInt(this.props.getProperty("redis-port", "6379")),
                Integer.parseInt(this.props.getProperty("redis-db", "0"))
        );
    }

    private PacketTracerInstanceProperties getConnectionChunks(String s) {
        if (s==null) return null;
        if (s.contains(":")) {
            final String[] chunks = s.trim().split(":");
            if (chunks.length!=2) {
                LOGGER.error("Incorrect instance name error: " + s + ".");
                return null;
            }
            final int port = Integer.parseInt(chunks[1]);
            return new PacketTracerInstanceProperties(chunks[0], port);
        } else {
            return new PacketTracerInstanceProperties(s.trim(), 39000);
        }
    }

    public PacketTracerInstanceProperties getSharedInstanceDetails() {
        return getConnectionChunks(this.props.getProperty("pt-shared-instance"));
    }

    public Set<String> getPacketTracerManagementAPIs() {
        final Set<String> apis = new HashSet<String>();
        final String unparsed = this.props.getProperty("pt-apis", PropertyFileManager.defaultAPI);
        if (unparsed.contains(",")) {
            for(String api: unparsed.split(",")) {
                try {
                    apis.add(api);
                } catch(Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }
        } else {
            try {
                apis.add(unparsed);
            } catch(Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        return apis;
    }

    /**
     * By default, the Redis server runs in the same machine as the web server.
     */
    public InteractionRecordingProperties getInteractionRecordingDetails() {
        final String filePath = this.props.getProperty("la-property-file", "False");
        if (filePath.toLowerCase().equals("false")) return null;
        try {
            final Properties interactionProps = new Properties();
            interactionProps.load(PropertyFileManager.class.getClassLoader().getResourceAsStream(filePath));
            return new InteractionRecordingProperties(
                        interactionProps.getProperty("endpoint"),
                        interactionProps.getProperty("username"),
                        interactionProps.getProperty("password")
                    );
        } catch(IOException e) {
            LOGGER.error("The interaction recording property file could not be read.", e);
            return null;
        } catch(AssertionError e) {
            LOGGER.error("One of the interaction properties was invalid or was not found.", e);
            return null;
        }
    }
}

package uk.ac.open.kmi.forge.webPacketTracer.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class PropertyFileManager {

    private static final Log LOGGER = LogFactory.getLog(PropertyFileManager.class);

    public static final String defaultHostname = "localhost";
    public static final int defaultPTPort = 39000;

    Properties props = new Properties();

    public PropertyFileManager() {
        try {
            this.props.load(PropertyFileManager.class.getClassLoader().getResourceAsStream("environment.properties"));
        } catch(IOException e) {
            LOGGER.error("The Redis connection details could not be read from the properties file, using default value.");
        }
    }

    /**
     * By default, the Redis server runs in the same machine as the web server.
     */
    public  RedisConnectionProperties getRedisConnectionDetails() {
        return new RedisConnectionProperties(
                this.props.getProperty("redis-host", PropertyFileManager.defaultHostname),
                Integer.parseInt(props.getProperty("redis-port", "6379")),
                Integer.parseInt(props.getProperty("redis-db", "0"))
        );
    }

    private PacketTracerInstanceProperties getConnectionChunks(String s) throws Exception {
        if (s.contains(":")) {
            final String[] chunks = s.trim().split(":");
            if (chunks.length!=2) {
                throw new Exception("Incorrect instance name error: " + s + ".");
            }
            final int port = Integer.parseInt(chunks[1]);
            return new PacketTracerInstanceProperties(chunks[0], port);
        } else {
            return new PacketTracerInstanceProperties(s.trim(), 39000);
        }
    }

    public Set<PacketTracerInstanceProperties> getPacketTracerInstancesDetails() {
        final Set<PacketTracerInstanceProperties> instances = new HashSet<PacketTracerInstanceProperties>();
        final String unparsed = this.props.getProperty("pt-instances", PropertyFileManager.defaultHostname + ":" + PropertyFileManager.defaultPTPort);
        if (unparsed.contains(",")) {
            for(String instance: unparsed.split(",")) {
                try {
                    instances.add(getConnectionChunks(instance));
                } catch(Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }
        } else {
            try {
                instances.add(getConnectionChunks(unparsed));
            } catch(Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        return instances;
    }
}

package uk.ac.open.kmi.forge.webPacketTracer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Utility class for HTTP API.
 */
public class Utils {

    private static final Log LOGGER = LogFactory.getLog(Utils.class);

    public static String unescapePort(String portName) {
        // FIXME: Issue with names containing slashes or backslashes and tomcat6.
        // http://stackoverflow.com/questions/2291428/jax-rs-pathparam-how-to-pass-a-string-with-slashes-hyphens-equals-too
        // To overcome it, I replaced slashes with spaces...
        try {
            portName = URLDecoder.decode(portName, "UTF-8");
            return portName.replace(" ", "/");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Apparently UTF-8 does not exist as an encoding :-S", e);
            return null;
        }
    }
}
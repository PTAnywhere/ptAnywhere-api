package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import com.cisco.pt.UUID;
import com.cisco.pt.impl.UUIDImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;

/**
 * Utility class for HTTP API.
 */
public class Utils {

    private static final Log LOGGER = LogFactory.getLog(Utils.class);

    /**
     * Simplifies UUIDs to remove dashes and curly braces.
     * @param uuid
     *  Examples: "{a9101f6b-ef7c-4372-91c2-9391e94ee233}", "6fc7797b-1a33-4fd7-8db1-1d6e7468db65"
     * @return
     *  Examples: "a9101f6bef7c437291c29391e94ee233", "6fc7797b1a334fd78db11d6e7468db65"
     */
    public static String toSimplifiedUUID(String uuid) {
        if (uuid==null) return uuid;
        return uuid.replaceAll("[^0-9a-f]", "");
    }

    public static String toSimplifiedUUID(UUID uuid) {
        return toSimplifiedUUID(uuid.getDecoratedHexString());
    }

    /**
     * Converts simplified UUIDs into Cisco's PacketTracer valid ID.
     * @param simplifiedUuid
     *  Examples: "a9101f6bef7c437291c29391e94ee233", "6fc7797b1a334fd78db11d6e7468db65"
     * @return
     *  Examples: "{a9101f6b-ef7c-4372-91c2-9391e94ee233}", "{6fc7797b-1a33-4fd7-8db1-1d6e7468db65}"
     */
    public static UUID toCiscoUUID(String simplifiedUuid) {
        return new UUIDImpl(simplifiedUuid);
    }

    public static String toUUID(String simplifiedUuid) {
        // TODO assert N digits?
        return simplifiedUuid.replaceAll(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5");
    }

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

    public static String escapePort(String portName) {
        return encodeForURL(portName.replace("/", " "));
    }

    public static String encodeForURL(String id) {
        try {
            return URLEncoder.encode(id, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Apparently UTF-8 does not exist as an encoding :-S", e);
            return null;
        }
    }

    public static URI getParent(URI uri) {
        return uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
    }

    public static String getURIWithSlashRemovingQuery(URI uri) {
        String ret = uri.toString();
        final int i = ret.indexOf("?");
        if (i!=-1) ret = ret.substring(0, i);
        if (ret.endsWith("/")) return ret;
        return ret + "/";
    }

    /**
     * Utility method to generate an array with Strings.
     *
     * (This is needed because, MOXy seems unable to generate them for String collections or arrays.
     *      return Response.ok(stringArray);  <-- This throws an error!
     * )
     * @param stringArray
     * @return
     *      A string representing a JSON array which contains String items.
     */
    public static String toJsonStringArray(Collection<String> stringArray) {
        String ret = "[";
        boolean first = false;
        for(String s: stringArray) {
            if (!first) {
                first = true;
            } else {
                ret += ", ";
            }
            ret += "\"" + s + "\"";
        }
        ret += "]";
        return ret;
    }
}
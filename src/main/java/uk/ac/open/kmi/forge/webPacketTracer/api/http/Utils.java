package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import com.cisco.pt.UUID;
import com.cisco.pt.impl.UUIDImpl;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.Charset;
import java.util.Collection;


/**
 * Utility class for HTTP API.
 */
public class Utils {

    private static final Log LOGGER = LogFactory.getLog(Utils.class);

    /**
     * Simplifies UUIDs converting them to base64url's Y64 variant.
     * The idea and the code was taken from:
     *   - http://stackoverflow.com/questions/21103363/base32-encode-uuid-in-java/21103563#21103563
     *
     * @param uuid
     *  Examples: "a9101f6b-ef7c-4372-91c2-9391e94ee233", "6fc7797b-1a33-4fd7-8db1-1d6e7468db65"
     * @return
     *  Examples: "qRAfa.98Q3KRwpOR6U7iMw--", "b8d5exozT9eNsR1udGjbZQ--"
     */
    public static String toSimplifiedId(java.util.UUID uuid) {
        ByteBuffer uuidBuffer = ByteBuffer.allocate(16);
        LongBuffer longBuffer = uuidBuffer.asLongBuffer();
        longBuffer.put(uuid.getMostSignificantBits());
        longBuffer.put(uuid.getLeastSignificantBits());
        String encoded = new String(Base64.encodeBase64(uuidBuffer.array()),
                Charset.forName("US-ASCII"));
        return encoded.replace('+', '.')
                .replace('/', '_')
                .replace('=', '-');
    }

    /**
     * Simplifies Cisco's UUIDs converting them to base64url's Y64 variant.
     * The idea and the code was taken from:
     *   - http://stackoverflow.com/questions/21103363/base32-encode-uuid-in-java/21103563#21103563
     *
     * @param uuid
     *  Examples: "{a9101f6b-ef7c-4372-91c2-9391e94ee233}", "{6fc7797b-1a33-4fd7-8db1-1d6e7468db65}"
     * @return
     *  Examples: "qRAfa.98Q3KRwpOR6U7iMw--", "b8d5exozT9eNsR1udGjbZQ--"
     */
    public static String toSimplifiedId(UUID uuid) {
        // Simple solution which might be slightly slow and memory demanding
        // than other solutions since it creates a intermediate java UUID object.
        final String decorated = uuid.getDecoratedHexString();
        return toSimplifiedId(java.util.UUID.fromString(decorated.substring(1, decorated.length()-1)) );
    }

    /**
     * Converts string ins base64url's Y64 variant to UUID.
     * The idea and the code was taken from:
     *   - http://stackoverflow.com/questions/21103363/base32-encode-uuid-in-java/21103563#21103563
     *
     * @param simplifiedId
     *  Examples: "qRAfa.98Q3KRwpOR6U7iMw--", "b8d5exozT9eNsR1udGjbZQ--"
     * @return
     *  Examples: "a9101f6b-ef7c-4372-91c2-9391e94ee233", "6fc7797b-1a33-4fd7-8db1-1d6e7468db65"
     */
    public static java.util.UUID toUUID(String simplifiedId) {
        final String encoded = simplifiedId.replace('.', '+')
                .replace('_', '/')
                .replace('-', '=');
        ByteBuffer uuidBuffer = ByteBuffer.wrap(Base64.decodeBase64(
                encoded.getBytes(Charset.forName("US-ASCII"))));
        LongBuffer longBuffer = uuidBuffer.asLongBuffer();
        return new java.util.UUID(longBuffer.get(), longBuffer.get());
    }

    /**
     * Converts string ins base64url's Y64 variant to Cisco's UUID.
     * The idea and the code was taken from:
     *   - http://stackoverflow.com/questions/21103363/base32-encode-uuid-in-java/21103563#21103563
     *
     * @param simplifiedId
     *  Examples: "qRAfa.98Q3KRwpOR6U7iMw--", "b8d5exozT9eNsR1udGjbZQ--"
     * @return
     *  Examples: "{a9101f6b-ef7c-4372-91c2-9391e94ee233}", "{6fc7797b-1a33-4fd7-8db1-1d6e7468db65}"
     */
    public static UUID toCiscoUUID(String simplifiedId) {
        return new UUIDImpl(toUUID(simplifiedId).toString());
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
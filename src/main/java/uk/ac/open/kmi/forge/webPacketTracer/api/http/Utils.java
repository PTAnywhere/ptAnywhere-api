package uk.ac.open.kmi.forge.webPacketTracer.api.http;

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
package uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab;

/**
 * For a more comprehensive and human-oriented description of the vocabulary used to describe interactions,
 * please go to [1].
 *
 * [1] https://github.com/PTAnywhere/ptAnywhere-api/wiki/Vocabulary-used-to-capture-user-interaction
 */
public class BaseVocabulary {
    // Own vocabulary
    private static final String VOCAB = "http://ict-forge.eu/vocab";
    public static final String PTANYWHERE = "http://pt-anywhere.kmi.open.ac.uk";  // For PTAnywhere related extensions
    public static final String UNKNOWN_WIDGET = VOCAB + "/widgets/unknown";

    /* Verbs */
    public static final String INITIALIZED = "http://adlnet.gov/expapi/verbs/initialized";
    public static final String TERMINATED = "http://adlnet.gov/expapi/verbs/terminated";
    public static final String CREATED = "http://activitystrea.ms/schema/1.0/create";
    public static final String DELETED = "http://activitystrea.ms/schema/1.0/delete";
    public static final String UPDATED = "http://activitystrea.ms/schema/1.0/update";
    // Verbs used with command line activities:
    public static final String OPENED = "http://activitystrea.ms/schema/1.0/open";
    public static final String CLOSED = "http://activitystrea.ms/schema/1.0/close";
    public static final String USED = "http://activitystrea.ms/schema/1.0/use";
    public static final String READ = "http://activitystrea.ms/schema/1.0/read";

    /* Objects */
    /** Objects -> Actitivies **/
    public static final String SIMULATION = "http://adlnet.gov/expapi/activities/simulation";
    public static final String ACTIVITIES = VOCAB + "/activities";
    public static final String SIMULATED_DEVICE = ACTIVITIES + "/device";
    public static final String SIMULATED_PORT = ACTIVITIES + "/port";
    public static final String SIMULATED_LINK = ACTIVITIES + "/link";
    public static final String COMMAND_LINE = ACTIVITIES + "/command-line";

    /* Extensions */
    private static final String EXTENSION = PTANYWHERE + "/extensions";
    public static final String EXT_DEVICE_NAME = EXTENSION + "/device/name";
    public static final String EXT_DEVICE_TYPE = EXTENSION + "/device/type";
    public static final String EXT_DEVICE_GW = EXTENSION + "/device/defaultGateway";
    public static final String EXT_DEVICE_URI = EXTENSION + "/device/uri";  // URI in the context of PTAnywhere API
    public static final String EXT_DEVICE_POSITION = EXTENSION + "/device/position";
    public static final String EXT_PORT_URI = EXTENSION + "/port/uri";
    public static final String EXT_PORT_NAME = EXTENSION + "/port/name";
    public static final String EXT_PORT_IP_ADDR = EXTENSION + "/port/ipAddress";
    public static final String EXT_PORT_SUBNET_MASK = EXTENSION + "/port/subnetMask";
    public static final String EXT_ENDPOINTS = EXTENSION + "/endpoints";
    public static final String EXT_LINK_URI = EXTENSION + "/link/uri";

    // In JSON objects
    public static final String EXT_ENDPOINT_DEVICE = "device";
    public static final String EXT_ENDPOINT_PORT = "port";
    public static final String EXT_POSITION_X = "x";
    public static final String EXT_POSITION_Y = "y";
}

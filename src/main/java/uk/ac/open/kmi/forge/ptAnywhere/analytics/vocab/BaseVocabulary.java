package uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab;


public class BaseVocabulary {
    // Own vocabulary
    private static final String VOCAB = "http://ict-forge.eu/vocab";
    public static final String PTANYWHERE = "http://pt-anywhere.kmi.open.ac.uk";  // For PTAnywhere related extensions

    // Activity http://adlnet.gov/expapi/activities/simulation

    /* Verbs */
    public static final String INITIALIZED = "http://adlnet.gov/expapi/verbs/initialized";
    public static final String TERMINATED = "http://adlnet.gov/expapi/verbs/terminated";
    public static final String CREATED = "http://activitystrea.ms/schema/1.0/create";
    public static final String DELETED = "http://activitystrea.ms/schema/1.0/delete";
    public static final String UPDATED = "http://activitystrea.ms/schema/1.0/update";
    // For command line, we could also register: "open" or "close"
    public static final String USED = "http://activitystrea.ms/schema/1.0/use";

    /* Objects */
    /** Objects -> Actitivies **/
    public static final String SIMULATION = "http://adlnet.gov/expapi/activities/simulation";
    public static final String ACTIVITIES = VOCAB + "/activities";
    public static final String SIMULATED_DEVICE = ACTIVITIES + "/device";
    public static final String SIMULATED_LINK = ACTIVITIES + "/link";
    public static final String COMMAND_LINE = ACTIVITIES + "/command-line";

    /* Extensions */
    private static final String EXTENSION = PTANYWHERE + "/extensions";
    public static final String EXT_DEVICE_NAME = EXTENSION + "/device/name";
    public static final String EXT_DEVICE_TYPE = EXTENSION + "/device/type";
    public static final String EXT_DEVICE_URI = EXTENSION + "/device/uri";  // URI in the context of PTAnywhere API
    public static final String EXT_DEVICE_POSITION = EXTENSION + "/device/position";
    public static final String EXT_ENDPOINTS = EXTENSION + "/endpoints";
    public static final String EXT_LINK_URI = EXTENSION + "/link/uri";

    // In JSON objects
    public static final String EXT_ENDPOINT_DEVICE = "device";
    public static final String EXT_ENDPOINT_PORT = "port";
    public static final String EXT_POSITION_X = "x";
    public static final String EXT_POSITION_Y = "y";
}

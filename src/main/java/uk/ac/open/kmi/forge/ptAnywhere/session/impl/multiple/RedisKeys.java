package uk.ac.open.kmi.forge.ptAnywhere.session.impl.multiple;


public interface RedisKeys {

    String AVAILABLE_APIS = "apis";
    // TODO use subscriptions to ensure that after deleting a busy-instance-key it is inserted again in the list of available ones.
    String INSTANCE_URL = "url";
    String INSTANCE_HOSTNAME = "hostname";
    String INSTANCE_PORT = "port";
    String INSTANCE_INPUT_FILE = "input_file";


    /**
     * List of IDs of session that ever existed
     */
    String SESSION_PREFIX = "session:";
    String URL_PREFIX = INSTANCE_URL + ":";
}

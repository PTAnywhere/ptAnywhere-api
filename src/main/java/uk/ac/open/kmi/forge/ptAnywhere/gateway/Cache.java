package uk.ac.open.kmi.forge.ptAnywhere.gateway;


/**
 * This cache is used to store mappings between device identifiers and names.
 *
 * This way, we can avoid making some additional requests to Packet Tracer.
 * For more information on the motivations to use this cache read the following issue:
 *      https://github.com/PTAnywhere/ptAnywhere-api/issues/9
 */
public interface Cache {

    void add(String networkId, String identifier, String name);
    void remove(String networkId, String identifier);
    void removeAll(String networkId);
    /**
     * @param identifier
     * @return
     *      The name associated to the given identifier or null if it cannot be determined.
     *      The reasons for a non resolution can be (1) that the identifier is not cached or
     *      (2) that there is one (or more) additional identifier associated to the same name.
     *      If the latter happens, the ID will be needed to retrieve this given device from Packet Tracer later on.
     */
    String getName(String networkId, String identifier);
}
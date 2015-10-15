package uk.ac.open.kmi.forge.ptAnywhere.session;

import uk.ac.open.kmi.forge.ptAnywhere.exceptions.NoPTInstanceAvailableException;

import java.util.Set;

/**
 * Created by agg96 on 10/15/15.
 */
public interface SessionsManager {
    void clear();

    void addManagementAPIs(String... apiUrls);

    String createSession() throws NoPTInstanceAvailableException;

    Set<String> getCurrentSessions();

    PTInstanceDetails getInstance(String sessionId);

    boolean doesExist(String sessionId);

    void deleteSession(String sessionId);

    /* Methods to ease webapp management */
    Set<PTInstanceDetails> getAllInstances();
}

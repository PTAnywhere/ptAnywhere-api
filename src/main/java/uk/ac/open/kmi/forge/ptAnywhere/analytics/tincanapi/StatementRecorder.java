package uk.ac.open.kmi.forge.ptAnywhere.analytics.tincanapi;

import com.rusticisoftware.tincan.Statement;

/**
 * It handles sending the requests to the TinCan API endpoint.
 */
public interface StatementRecorder {
    void record(final Statement statement);
}

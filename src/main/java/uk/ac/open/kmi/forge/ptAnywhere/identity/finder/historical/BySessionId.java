package uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical;

import uk.ac.open.kmi.forge.ptAnywhere.identity.finder.FindingCriterion;


/**
 * Looks for previous sessions' identities.
 */
public class BySessionId implements FindingCriterion {
    final String formerSessionId;

    public BySessionId(String formerSessionId) {
        this.formerSessionId = formerSessionId;
    }

    public String getPreviousSessionId() {
        return this.formerSessionId;
    }
}
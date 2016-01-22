package uk.ac.open.kmi.forge.ptAnywhere.identity.finder.historical.lrs;

import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.AgentAccount;
import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;


/**
 * This class represent an identity which has already existed in a previous session/registration.
 */
public class HistoricalIdentity implements Identifiable {
    final String name;
    final AgentAccount account;

    protected HistoricalIdentity(Agent account) {
        this.name = account.getName();
        this.account = account.getAccount();
    }

    public String getName() {
        return this.name;
    }

    public String getHomePage() {
        return this.account.getHomePage();
    }

    public String getAccountName() {
        return this.account.getName();
    }
}

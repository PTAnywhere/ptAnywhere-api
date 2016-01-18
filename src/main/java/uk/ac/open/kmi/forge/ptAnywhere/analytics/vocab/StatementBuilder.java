package uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab;

import com.rusticisoftware.tincan.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.URIFactory;
import uk.ac.open.kmi.forge.ptAnywhere.identity.Identifiable;

import java.net.URISyntaxException;


public class StatementBuilder {

    private static final Log LOGGER = LogFactory.getLog(StatementBuilder.class);

    final URIFactory factory;
    final Agent agent = new Agent();
    final Verb verb = new Verb();
    final PTActivityBuilder activityBuilder;
    final PTResultBuilder resultBuilder;
    final PTContextBuilder contextBuilder;

    public StatementBuilder(URIFactory factory) {
        this.factory = factory;
        this.activityBuilder = new PTActivityBuilder(this.factory);
        this.resultBuilder =  new PTResultBuilder();
        this.contextBuilder = new PTContextBuilder(this.factory);
    }

    public StatementBuilder student(Identifiable identity) {
        if (identity==null) {
            // TODO treat it properly as TinCanAPI might not accept a null agent
            LOGGER.error("The identity provided was not valid.");
            LOGGER.error("The identity is null, which means that (1) it was not properly set or " +
                         "(2) the IdentityFinder did not find it.");
        } else {
            final AgentAccount aa = new AgentAccount();
            aa.setHomePage(identity.getHomePage());
            aa.setName(identity.getName());
            this.agent.setAccount(aa);
        }
        return this;
    }

    public StatementBuilder verb(String verbUri) throws URISyntaxException {
        this.verb.setId(verbUri);
        return this;
    }

    public PTActivityBuilder getActivityBuilder() {
        return this.activityBuilder;
    }

    public PTContextBuilder getContextBuilder() {
        return this.contextBuilder;
    }

    public PTResultBuilder getResultBuilder() {
        return this.resultBuilder;
    }

    public Statement build() {
        final Statement stmt = new Statement();
        stmt.setActor(this.agent);
        stmt.setVerb(this.verb);
        stmt.setObject(this.activityBuilder.build());
        if (this.resultBuilder.build()!=null)
            stmt.setResult(this.resultBuilder.build());
        stmt.setContext(this.contextBuilder.build());
        return stmt;
    }
}

package uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab;

import com.rusticisoftware.tincan.*;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.URIFactory;
import java.net.URISyntaxException;


public class StatementBuilder {

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

    public StatementBuilder anonymousUser(String sessionId) {
        final AgentAccount aa = new AgentAccount();
        // This could be set to the real URL where the app is deployed.
        aa.setHomePage("http://forge.kmi.open.ac.uk/pt/widget");
        aa.setName("anonymous_" + sessionId);
        this.agent.setAccount(aa);
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

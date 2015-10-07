package uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab;

import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.ContextActivities;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.URIFactory;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.Utils;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class PTContextBuilder {

    final Context context;
    final URIFactory factory;

    PTContextBuilder(URIFactory factory) {
        this.factory = factory;
        this.context = new Context();
    }

    /*
     * Right now, this information is unnecessary as the user can be used
     * for reporting sessions.
     *  However, in the future users will be de-anonymized.
     */
    public PTContextBuilder addSession(String sessionId) {
        this.context.setRegistration(Utils.toUUID(sessionId));
        return this;
    }

    public PTContextBuilder addParentActivity() throws URISyntaxException {
        final PTActivityBuilder actBuilder = new PTActivityBuilder(this.factory);
        final List<Activity> parents = new ArrayList<Activity>();
        parents.add(actBuilder.widgetActivity().build());
        final ContextActivities ca = new ContextActivities();
        ca.setParent(parents);
        this.context.setContextActivities(ca);
        return this;
    }

    public Context build() {
        return this.context;
    }
}

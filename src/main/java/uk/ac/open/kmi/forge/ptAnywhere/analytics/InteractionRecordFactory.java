package uk.ac.open.kmi.forge.ptAnywhere.analytics;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.properties.InteractionRecordingProperties;


public class InteractionRecordFactory {

    private static final Log LOGGER = LogFactory.getLog(InteractionRecordFactory.class);

    private final ExecutorService executor;  // This executor is not handled by this class.
    private final InteractionRecordingProperties irp;

    public InteractionRecordFactory(ExecutorService executor, InteractionRecordingProperties props) {
        this.executor = executor;
        this.irp = props;
    }

    protected InteractionRecord create() {
        if (this.irp==null) new NoTracker();
        try {
            return new TinCanAPI(this.irp.getEndpoint(), this.irp.getUsername(), this.irp.getPassword(), this.executor);
        } catch(MalformedURLException e) {
            LOGGER.error(e.getMessage());
            return new NoTracker();
        }
    }

    public InteractionRecord create(String widgetURI, String sessionId) {
        final InteractionRecord ir = create();
        ir.setURIFactory(new URIFactory(widgetURI));
        ir.setSession(sessionId);
        return ir;
    }
}
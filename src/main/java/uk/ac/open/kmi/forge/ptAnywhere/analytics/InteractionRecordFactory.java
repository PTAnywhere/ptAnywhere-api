package uk.ac.open.kmi.forge.ptAnywhere.analytics;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.properties.InteractionRecordingProperties;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PropertyFileManager;


public class InteractionRecordFactory {

    private static final Log LOGGER = LogFactory.getLog(InteractionRecordFactory.class);

    private final ExecutorService executor;  // This executor is not handled by this class.
    private final InteractionRecordingProperties irp;

    public InteractionRecordFactory(ExecutorService executor) {
        this.executor = executor;
        // Only an object is created per application, so we are not reading the file over and over again.
        final PropertyFileManager pfm = new PropertyFileManager();
        this.irp = pfm.getInteractionRecordingDetails();
    }

    public InteractionRecord create() {
        if (this.irp==null) new NoTracker();
        try {
            return new TinCanAPI(this.irp.getEndpoint(), this.irp.getUsername(), this.irp.getPassword(), this.executor);
        } catch(MalformedURLException e) {
            LOGGER.error(e.getMessage());
            return new NoTracker();
        }
    }
}
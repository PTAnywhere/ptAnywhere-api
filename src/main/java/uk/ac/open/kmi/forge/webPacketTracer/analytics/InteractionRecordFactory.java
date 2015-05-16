package uk.ac.open.kmi.forge.webPacketTracer.analytics;

import java.net.MalformedURLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.webPacketTracer.properties.InteractionRecordingProperties;
import uk.ac.open.kmi.forge.webPacketTracer.properties.PropertyFileManager;


public class InteractionRecordFactory {

    private static final Log LOGGER = LogFactory.getLog(InteractionRecordFactory.class);

    public static InteractionRecordable create() {
        // FIXME should we cache these values to avoid reading the file over and over again???
        final PropertyFileManager pfm = new PropertyFileManager();
        final InteractionRecordingProperties irp = pfm.getInteractionRecordingDetails();
        if (irp==null) new NoTracking();
        try {
            return new TinCanAPI(irp.getEndpoint(), irp.getUsername(), irp.getPassword());
        } catch(MalformedURLException e) {
            LOGGER.error(e.getMessage());
            return new NoTracking();
        }
    }
}

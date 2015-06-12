package uk.ac.open.kmi.forge.webPacketTracer.analytics;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.webPacketTracer.properties.InteractionRecordingProperties;
import uk.ac.open.kmi.forge.webPacketTracer.properties.PropertyFileManager;


public class InteractionRecordFactory {

    private static final Log LOGGER = LogFactory.getLog(InteractionRecordFactory.class);

    // Right we only use it to ensure that the TinCanAPI clients execute in their own threads.
    // But if in the future this executor does other type of tasks,
    // we should consider moving its management to the Application object.
    private ExecutorService executor = null;

    private final InteractionRecordingProperties irp;

    public InteractionRecordFactory() {
        // Only an object is created per application, so we are not reading the file over and over again.
        final PropertyFileManager pfm = new PropertyFileManager();
        this.irp = pfm.getInteractionRecordingDetails();
    }

    private ExecutorService getExecutor() {
        // Lazy creation to prevent NoTracker record from unnecessarily creating it.
        if (this.executor!=null) {
            return this.executor;
        }
        // TODO adjust number of threads
        this.executor = Executors.newFixedThreadPool(10, new SimpleDaemonFactory());
        return this.executor;
    }

    public InteractionRecord create() {
        if (this.irp==null) new NoTracker();
        try {
            return new TinCanAPI(this.irp.getEndpoint(), this.irp.getUsername(), this.irp.getPassword(), getExecutor());
        } catch(MalformedURLException e) {
            LOGGER.error(e.getMessage());
            return new NoTracker();
        }
    }

    public void shutdown() {
        LOGGER.debug("Destroying InteractionRecordFactory.");
        this.executor.shutdownNow();
    }
}


// Following the advice from:
//   http://stackoverflow.com/questions/3745905/what-is-recommended-way-for-spawning-threads-from-a-servlet-in-tomcat
class SimpleDaemonFactory implements ThreadFactory {
    public Thread newThread(Runnable r) {
        final Thread t =new Thread(r);
        t.setDaemon(true);
        return t;
    }
}
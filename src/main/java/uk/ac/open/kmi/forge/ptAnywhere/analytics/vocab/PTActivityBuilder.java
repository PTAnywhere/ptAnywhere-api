package uk.ac.open.kmi.forge.ptAnywhere.analytics.vocab;

import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.ActivityDefinition;
import com.rusticisoftware.tincan.LanguageMap;
import uk.ac.open.kmi.forge.ptAnywhere.analytics.URIFactory;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.net.URI;
import java.net.URISyntaxException;


// Always the same structure
public class PTActivityBuilder {

    private URIFactory factory;
    final Activity activity = new Activity();

    PTActivityBuilder(URIFactory factory) {
        this.factory = factory;
    }

    public PTActivityBuilder widgetActivity() throws URISyntaxException {
        this.activity.setId(this.factory.getWidgetURI());
        final ActivityDefinition definition = new ActivityDefinition();
        definition.setType(BaseVocabulary.SIMULATION);
        this.activity.setDefinition(definition);
        return this;
    }

    public PTActivityBuilder simulatedDevice(String deviceType) throws URISyntaxException {
        this.activity.setId(BaseVocabulary.SIMULATED_DEVICE + "/" + deviceType);
        final LanguageMap lm = new LanguageMap();
        lm.put("en-GB", "Simulated " + deviceType);
        final ActivityDefinition definition = new ActivityDefinition();
        definition.setName(lm);
        definition.setType(BaseVocabulary.SIMULATION);
        this.activity.setDefinition(definition);
        return this;
    }

    public PTActivityBuilder simulatedPort(String deviceName, String portName) throws URISyntaxException {
        final LanguageMap lm = new LanguageMap();
        lm.put("en-GB", deviceName + "'s port " + portName);
        final ActivityDefinition definition = new ActivityDefinition();
        definition.setName(lm);
        definition.setType(BaseVocabulary.SIMULATED_PORT);
        // It would be particularly useful to refer to activity types across sessions
        //   (e.g., modifying X port were X is always the same port of the same device)
        final URI clUri = UriBuilder.fromPath(this.factory.getDeviceURI(deviceName)).path("port").path(portName).build();
        this.activity.setId(clUri);
        this.activity.setDefinition(definition);
        return this;
    }

    public PTActivityBuilder simulatedLink() throws URISyntaxException {
        this.activity.setId(BaseVocabulary.SIMULATED_LINK);
        final LanguageMap lm = new LanguageMap();
        lm.put("en-GB", "Link"); // Generic name defined to enhance readability in LearningLocker.
        final ActivityDefinition definition = new ActivityDefinition();
        definition.setName(lm);
        definition.setType(BaseVocabulary.SIMULATION);
        this.activity.setDefinition(definition);
        return this;
    }

    public PTActivityBuilder commandLineActivity(String deviceName) throws URISyntaxException,
            IllegalArgumentException, UriBuilderException {
        final LanguageMap lm = new LanguageMap();
        lm.put("en-GB", deviceName + "'s command line");
        final ActivityDefinition definition = new ActivityDefinition();
        definition.setName(lm);
        definition.setType(BaseVocabulary.COMMAND_LINE);
        // It would be particularly useful to refer to activity types across sessions
        //   (e.g., opening X console were X is always the same id)
        final URI clUri = UriBuilder.fromPath(this.factory.getDeviceURI(deviceName)).path("console").build();
        this.activity.setId(clUri);
        this.activity.setDefinition(definition);
        return this;
    }

    public Activity build() {
        return this.activity;
    }
}

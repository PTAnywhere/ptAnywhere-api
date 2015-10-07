package uk.ac.open.kmi.forge.ptAnywhere.analytics;


/**
 * URI factory different from the one in "uk.ac.open.kmi.forge.ptAnywhere.api".
 *
 * The main feature for this one is that is generates equivalent element URIs across sessions.
 *
 * Somebody would expect to query the LRS things like: "sessions opened in device 1",
 * where "device 1" is in each session of the widget
 * (compare this to "sessions opened in device random-unique-id").
 */
public class URIFactory {

    final String widgetUri;

    public URIFactory(String widgetUri) {
        this.widgetUri = (widgetUri.endsWith("/"))? widgetUri: widgetUri + "/";
    }

    public String getWidgetURI() {
        return this.widgetUri;
    }

    public String getDeviceURI(String deviceName) {
        return this.widgetUri + "device/" + deviceName.hashCode();
    }

}

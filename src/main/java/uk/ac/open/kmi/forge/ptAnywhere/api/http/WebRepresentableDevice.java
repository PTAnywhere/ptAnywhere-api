package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import uk.ac.open.kmi.forge.ptAnywhere.pojo.Device;


public abstract class WebRepresentableDevice extends AbstractWebRepresentable<Device>  {

    // Full URL of the resource.
    // It must also be the main identifier for the HTTP API clients.
    public abstract String getConsoleEndpoint();

    public void setConsoleEndpoint(String url) {
        /* FAKE implementation just to ensure that the POJO is created. */
    }
}

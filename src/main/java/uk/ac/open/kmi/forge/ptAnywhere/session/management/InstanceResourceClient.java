package uk.ac.open.kmi.forge.ptAnywhere.session.management;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;


public class InstanceResourceClient {
    final WebTarget target;

    public InstanceResourceClient(WebTarget target) {
        this.target = target;
    }

    public InstanceResourceClient(String instanceUrl) {
        this.target = ClientBuilder.newClient().target(instanceUrl);
    }

    public Instance delete() throws NotFoundException {
        return this.target
                .request(MediaType.APPLICATION_JSON)
                .delete(Instance.class);
    }
}
package uk.ac.open.kmi.forge.ptAnywhere.session.management;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;


public class AllocationResourceClient {
    final WebTarget target;

    public AllocationResourceClient(WebTarget target) {
        this.target = target;
    }

    public AllocationResourceClient(String instanceUrl) {
        this.target = ClientBuilder.newClient().target(instanceUrl);
    }

    public Allocation delete() throws NotFoundException {
        return this.target
                .request(MediaType.APPLICATION_JSON)
                .delete(Allocation.class);
    }
}
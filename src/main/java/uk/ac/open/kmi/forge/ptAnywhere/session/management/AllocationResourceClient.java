package uk.ac.open.kmi.forge.ptAnywhere.session.management;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;


public class AllocationResourceClient {
    final WebTarget target;

    // Only used by PTManagementClient
    protected AllocationResourceClient(WebTarget target) {
        this.target = target;
    }

    public AllocationResourceClient(String allocationUrl, Client client) {
        this.target = client.target(allocationUrl);
    }

    public Allocation delete() throws NotFoundException {
        return this.target
                .request(MediaType.APPLICATION_JSON)
                .delete(Allocation.class);
    }
}
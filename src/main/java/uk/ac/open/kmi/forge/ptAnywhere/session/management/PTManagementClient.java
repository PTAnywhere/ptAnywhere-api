package uk.ac.open.kmi.forge.ptAnywhere.session.management;

import uk.ac.open.kmi.forge.ptAnywhere.exceptions.NoPTInstanceAvailableException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.UnresolvableFileUrlException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class PTManagementClient {

    final static String ALLOCATIONS_PATH = "allocations";
    final static String ALLOCATION_PARAM = "allocationId";
    final static String FILES_PATH = "files";
    final static String ALLOCATION_PATH = ALLOCATIONS_PATH + "/{" + ALLOCATION_PARAM + "}";

    final WebTarget target;


    // For testing
    protected PTManagementClient(WebTarget target) {
        this.target = target;
    }

    public PTManagementClient(String managementApiUrl, Client client) {
        this.target = client.target(managementApiUrl);
    }

    public Allocation createInstance() throws NoPTInstanceAvailableException {
        final Response response = this.target.path(ALLOCATIONS_PATH)
                                                //.queryParam()
                                                .request(MediaType.APPLICATION_JSON)
                                                //.header("some-header", "true")
                                                .post(null);
        try {
            // List<Customer> customers = .get(new GenericType<List<Customer>>(){});
            if (response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode()) {
                throw new NoPTInstanceAvailableException(response.readEntity(PTManagementError.class).getMessage());
            } else if (response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
                throw new NoPTInstanceAvailableException(response.readEntity(PTManagementError.class).getMessage());
            }
            return response.readEntity(Allocation.class);
        } finally {
            response.close();
        }
    }

    public Allocation deleteInstance(int instanceId) throws NotFoundException {
        final AllocationResourceClient irc = new AllocationResourceClient(
                this.target.path(ALLOCATION_PATH.replace("{" + ALLOCATION_PARAM + "}", String.valueOf(instanceId))) );
        return irc.delete();
    }


    public File getCachedFile(String url) {
        final Response response = this.target.path(FILES_PATH)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(url, MediaType.TEXT_PLAIN));
        try {
            if (response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode() ||
                    response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
                //We could also reuse the message from the PTManagement API:
                /*final PTManagementError error = response.readEntity(PTManagementError.class);
                error.getMessage();*/
                throw new UnresolvableFileUrlException(url);
            }
            return response.readEntity(File.class);
        } finally {
            response.close();
        }
    }
}
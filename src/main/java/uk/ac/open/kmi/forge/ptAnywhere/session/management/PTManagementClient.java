package uk.ac.open.kmi.forge.ptAnywhere.session.management;

import uk.ac.open.kmi.forge.ptAnywhere.exceptions.ErrorBean;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.NoPTInstanceAvailableException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.UnresolvableFileUrlException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
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


    public PTManagementClient(WebTarget target) {
        this.target = target;
    }

    public PTManagementClient(String managementApiUrl) {
        final Client client = ClientBuilder.newClient();
        this.target = client.target(managementApiUrl);
    }

    public Allocation createInstance() throws NoPTInstanceAvailableException {
        /*  .path("resource/helloworld")
            .queryParam("greeting", "Hi World!")
            .request(MediaType.TEXT_PLAIN_TYPE)
            .header("some-header", "true")
            .get(String.class);
        */
        final Response response = this.target.path(ALLOCATIONS_PATH)
                //.queryParam()
                .request(MediaType.APPLICATION_JSON)
                        //.header("some-header", "true")
                .post(null);
        // List<Customer> customers = .get(new GenericType<List<Customer>>(){});
        if (response.getStatus()==Response.Status.SERVICE_UNAVAILABLE.getStatusCode()) {
            throw new NoPTInstanceAvailableException(response.readEntity(ErrorBean.class).getErrorMsg());
        }
        return response.readEntity(Allocation.class);
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
        if (response.getStatus()==Response.Status.BAD_REQUEST.getStatusCode()) {
            final ErrorBean error = response.readEntity(ErrorBean.class);
            throw new UnresolvableFileUrlException((error==null)? null: error.getErrorMsg());
        }
        return response.readEntity(File.class);
    }
}
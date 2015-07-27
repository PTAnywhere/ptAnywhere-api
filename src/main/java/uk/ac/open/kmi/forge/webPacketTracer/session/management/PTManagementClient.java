package uk.ac.open.kmi.forge.webPacketTracer.session.management;

import uk.ac.open.kmi.forge.webPacketTracer.api.http.exceptions.NoPTInstanceAvailableException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class PTManagementClient {

    final static String INSTANCES_PATH = "instances";
    final static String INSTANCE_PARAM = "instanceId";
    final static String INSTANCE_PATH = INSTANCES_PATH + "/{" + INSTANCE_PARAM + "}";

    final WebTarget target;


    public PTManagementClient(WebTarget target) {
        this.target = target;
    }

    public PTManagementClient(String managementApiUrl) {
        final Client client = ClientBuilder.newClient();
        this.target = client.target(managementApiUrl);
    }

    public Instance createInstance() throws NoPTInstanceAvailableException {
        /*  .path("resource/helloworld")
            .queryParam("greeting", "Hi World!")
            .request(MediaType.TEXT_PLAIN_TYPE)
            .header("some-header", "true")
            .get(String.class);
        */
        try {
            final Response response = this.target.path(INSTANCES_PATH)
                    //.queryParam()
                    .request(MediaType.APPLICATION_JSON)
                            //.header("some-header", "true")
                    .post(null);
            // List<Customer> customers = .get(new GenericType<List<Customer>>(){});
            return response.readEntity(Instance.class);
        } catch(ServiceUnavailableException e) {
            throw new NoPTInstanceAvailableException(e.getMessage());
        }
    }

    public Instance deleteInstance(int instanceId) throws NotFoundException {
        final InstanceResourceClient irc = new InstanceResourceClient(
                this.target.path(INSTANCE_PATH.replace("{" + INSTANCE_PARAM + "}", String.valueOf(instanceId))) );
        return irc.delete();
    }
}
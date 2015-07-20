package uk.ac.open.kmi.forge.webPacketTracer.session;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;


public class PTManagementClient {

    final WebTarget target;

    public PTManagementClient(String managementAPIURL) {
        final Client client = ClientBuilder.newClient();
        this.target = client.target(managementAPIURL);
    }

    public void createInstance() {
        /*  .path("resource/helloworld")
            .queryParam("greeting", "Hi World!")
            .request(MediaType.TEXT_PLAIN_TYPE)
            .header("some-header", "true")
            .get(String.class);
        * */
        final String response = this.target.path("instances")
            //.queryParam()
            .request(MediaType.APPLICATION_JSON)
            //.header("some-header", "true")
            .get(String.class);  // TODO
            // List<Customer> customers = .get(new GenericType<List<Customer>>(){});
    }
}
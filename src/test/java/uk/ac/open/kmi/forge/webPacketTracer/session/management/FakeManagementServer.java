import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import java.net.URI;


public class FakeManagementServer {

    static final String BASE_URI = "http://localhost:8080/myapp/";
    static HttpServer server;

    private static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages("where.my.resources.are")
                .register(HelloResource.class);
        // normally the resource class would not be in the unit test class
        // and would be in the `where.my.resources.are` package pr sub package
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), resourceConfig);
    }

    @Path("instances")
    public static class HelloResource {
        @GET
        public String getHello() {
            return "Hello World!";
        }
    }

    @BeforeClass
    public static void setUpClass() {
        server = startServer();
    }

    @AfterClass
    public static void tearDownClass() {
        server.shutdown();
    }

    @Test
    public void test() {
        WebTarget target = client.target(BASE_URI);
        Response response = target.path("hello").request().get();
        String hello = response.readEntity(String.class);
        assertEquals("Hello World!", hello);
        response.close();
    }
}
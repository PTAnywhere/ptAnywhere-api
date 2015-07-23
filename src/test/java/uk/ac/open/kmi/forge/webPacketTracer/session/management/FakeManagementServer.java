package uk.ac.open.kmi.forge.webPacketTracer.session.management;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static uk.ac.open.kmi.forge.webPacketTracer.session.management.PTManagementClient.*;


public class FakeManagementServer {

    final HttpServer server;
    static Instance createdInstance;
    static final Map<Integer, Instance> instances = new HashMap<Integer, Instance>();

    @Path(INSTANCES_PATH)
    public static class InstanceResource {
        @POST
        @Produces(MediaType.APPLICATION_JSON)
        public Response createInstance() {
            return Response.ok(createdInstance).build();
        }

        @DELETE @Path("/{" + INSTANCE_PARAM + "}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response deleteInstance(@PathParam(INSTANCE_PARAM) String instanceId) {
            final Instance deleted = instances.remove(Integer.parseInt(instanceId));
            if (deleted==null)
                return Response.status(Response.Status.NOT_FOUND).build();
            return Response.ok(deleted).build();
        }
    }

    public FakeManagementServer(String baseUri) {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .register(InstanceResource.class);
        this.server = GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), resourceConfig);
    }

    public void shutdown() {
        this.server.shutdown();
    }

    public void addInstance(Instance instance) {
        this.instances.put(instance.getId(), instance);
    }
}
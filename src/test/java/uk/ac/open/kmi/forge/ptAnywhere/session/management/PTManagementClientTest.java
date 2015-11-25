package uk.ac.open.kmi.forge.ptAnywhere.session.management;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.NoPTInstanceAvailableException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.UnresolvableFileUrlException;

import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static uk.ac.open.kmi.forge.ptAnywhere.session.management.PTManagementClient.FILES_PATH;
import static uk.ac.open.kmi.forge.ptAnywhere.session.management.PTManagementClient.ALLOCATIONS_PATH;
import static uk.ac.open.kmi.forge.ptAnywhere.session.management.PTManagementClient.ALLOCATION_PARAM;


public class PTManagementClientTest extends JerseyTest {

    PTManagementClient client;

    @Path(ALLOCATIONS_PATH)
    public static class FakeInstanceResource {
        static Allocation createdAllocation;  // If null, unavailable
        static final Map<Integer, Allocation> instances = new HashMap<Integer, Allocation>();

        @POST
        @Produces(MediaType.APPLICATION_JSON)
        public Response createInstance() {
            if (createdAllocation ==null) {
                throw new NoPTInstanceAvailableException();
            }
            return Response.ok(createdAllocation).build();
        }

        @DELETE
        @Path("/{" + ALLOCATION_PARAM + "}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response deleteInstance(@PathParam(ALLOCATION_PARAM) String instanceId) {
            final Allocation deleted = instances.remove(Integer.parseInt(instanceId));
            if (deleted==null)
                return Response.status(Response.Status.NOT_FOUND).build();
            return Response.ok(deleted).build();
        }

        public static void addInstance(Allocation allocation) {
            instances.put(allocation.getId(), allocation);
        }
    }

    @Path(FILES_PATH)
    public static class FakeFileResource {
        static String invalidUrl = "http://invalid";
        static String localFilename = "/data/local/renamedo.pkg";

        @POST
        @Produces(MediaType.APPLICATION_JSON)
        public Response cacheFile(String url) {
            if (url.equals(invalidUrl)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            return Response.ok(new File(url, localFilename)).build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(FakeInstanceResource.class, FakeFileResource.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.client = new PTManagementClient(target());
        FakeInstanceResource.createdAllocation = new Allocation(1, "http://localhost/inst/1", "localhost:39000", "today", "tomorrow");
    }

    @Test
    public void testCreateInstance() {
        assertEquals(FakeInstanceResource.createdAllocation, this.client.createInstance());
    }

    @Test(expected = NoPTInstanceAvailableException.class)
    public void testCreateInstanceUnavailable() {
        FakeInstanceResource.createdAllocation = null;
        this.client.createInstance();
    }

    @Test
    public void testDeleteInstance() {
        final Allocation expected = new Allocation(20, "http://localhost/inst/2", "localhost:39001", "today", "tomorrow");
        FakeInstanceResource.addInstance(expected);
        assertEquals(expected, this.client.deleteInstance(20));
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteInstanceNotFound() {
        this.client.deleteInstance(23);
    }

    @Test
    public void testGetCachedFile() {
        final String url = "http://amazing/resolvable/url/";
        final File cachedFile = this.client.getCachedFile(url);
        assertEquals(url, cachedFile.getUrl());
        assertEquals(FakeFileResource.localFilename, cachedFile.getFilename());
    }

    @Test(expected = UnresolvableFileUrlException.class)
    public void testGetCachedFileUnresolvable() {
        this.client.getCachedFile(FakeFileResource.invalidUrl);
    }
}

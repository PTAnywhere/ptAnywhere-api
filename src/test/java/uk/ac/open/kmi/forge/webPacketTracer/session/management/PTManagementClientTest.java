package uk.ac.open.kmi.forge.webPacketTracer.session.management;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.NotFoundException;

import static org.junit.Assert.*;


public class PTManagementClientTest {

    FakeManagementServer server;
    PTManagementClient client;

    @Before
    public void setUp() {
        final String baseUri = "http://localhost:8080/myapp/";
        this.server = new FakeManagementServer(baseUri);
        this.client = new PTManagementClient(baseUri);
        FakeManagementServer.createdInstance = new Instance(1, "http://localhost/inst/1", "dockerid1", 39000, "vnc://localhost:5901", "today", "tomorrow");
    }

    @After
    public void tearDown() {
        this.server.shutdown();
    }

    @Test
    public void tesCreateInstance() {
        assertEquals(FakeManagementServer.createdInstance, this.client.createInstance());
    }

    @Test
    public void testDeleteInstance() {
        final Instance expected = new Instance(20, "http://localhost/inst/2", "dockerid2", 39001, "vnc://localhost:5902", "today", "tomorrow");
        this.server.addInstance(expected);
        assertEquals(expected, this.client.deleteInstance(20));
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteInstanceNotFound() {
        this.client.deleteInstance(23);
    }
}

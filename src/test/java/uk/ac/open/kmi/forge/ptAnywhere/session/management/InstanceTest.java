package uk.ac.open.kmi.forge.ptAnywhere.session.management;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class InstanceTest {
    @Test
    public void testGetHostnameAndPort() {
        final Instance i = new Instance();
        i.setPacketTracer("localhost:39000");
        assertEquals("localhost", i.getPacketTracerHostname());
        assertEquals(39000, i.getPacketTracerPort());
    }
}
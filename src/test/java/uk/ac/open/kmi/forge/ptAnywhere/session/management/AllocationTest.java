package uk.ac.open.kmi.forge.ptAnywhere.session.management;

import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class AllocationTest {
    @Test
    public void testGetHostnameAndPort() {
        final Allocation i = new Allocation();
        i.setPacketTracer("localhost:39000");
        assertEquals("localhost", i.getPacketTracerHostname());
        assertEquals(39000, i.getPacketTracerPort());
    }
}
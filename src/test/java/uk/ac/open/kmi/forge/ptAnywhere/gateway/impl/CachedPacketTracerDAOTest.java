package uk.ac.open.kmi.forge.ptAnywhere.gateway.impl;

import com.cisco.pt.impl.UUIDImpl;
import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.ui.LogicalWorkspace;
import org.junit.Before;
import org.junit.Test;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.DeviceNotFoundException;

import static org.mockito.Mockito.*;


public class CachedPacketTracerDAOTest {

    final String uuid = "{a9101f6b-ef7c-4372-91c2-9391e94ee233}";
    final String simpleId = "b8d5exozT9eNsR1udGjbZQ--";

    MemoryCache cache;
    Network network;
    LogicalWorkspace workspace;
    CachedPacketTracerDAO tested;

    @Before
    public void setUp() {
        this.network = mock(Network.class);
        when(this.network.getObjectUUID()).thenReturn(new UUIDImpl(this.uuid));
        when(this.network.getDeviceCount()).thenReturn(0);
        this.workspace = mock(LogicalWorkspace.class);
        this.cache = new MemoryCache();
        this.tested = new CachedPacketTracerDAO(this.workspace, this.network, this.cache);
    }

    @Test(expected = DeviceNotFoundException.class)
    public void testGetSimDeviceById() {
        this.tested.getSimDeviceById(this.simpleId);
        verify(this.network).getDeviceCount();  // Not found in cache and then looked for it in network
    }

}

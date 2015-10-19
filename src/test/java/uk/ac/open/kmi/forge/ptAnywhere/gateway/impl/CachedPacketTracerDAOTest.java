package uk.ac.open.kmi.forge.ptAnywhere.gateway.impl;

import com.cisco.pt.impl.UUIDImpl;
import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.ui.LogicalWorkspace;
import org.junit.Before;
import org.junit.Test;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.DeviceNotFoundException;

import static org.mockito.Mockito.*;


public class CachedPacketTracerDAOTest {

    final String netUuid = "{a9101f6b-ef7c-4372-91c2-9391e94ee233}";
    final String netSimpleId = "qRAfa.98Q3KRwpOR6U7iMw--";
    final String dUuid = "{6fc7797b-1a33-4fd7-8db1-1d6e7468db65}";
    final String dSimpleId = "b8d5exozT9eNsR1udGjbZQ--";

    MemoryCache cache;
    Network network;
    LogicalWorkspace workspace;
    CachedPacketTracerDAO tested;

    @Before
    public void setUp() {
        this.network = mock(Network.class);
        when(this.network.getObjectUUID()).thenReturn(new UUIDImpl(this.netUuid));
        when(this.network.getDeviceCount()).thenReturn(0);
        this.workspace = mock(LogicalWorkspace.class);
        this.cache = new MemoryCache();
        this.tested = new CachedPacketTracerDAO(this.workspace, this.network, this.cache);
    }

    @Test(expected = DeviceNotFoundException.class)
    public void testGetSimDeviceByIdNotCached() {
        this.tested.getSimDeviceById(this.netSimpleId);
        verify(this.network).getDeviceCount();  // Not found in cache and then looked for it in network
    }

    @Test(expected = DeviceNotFoundException.class)
    public void testGetSimDeviceByIdCached() {
        this.cache.add(this.netSimpleId, this.dSimpleId, "device name");
        this.tested.getSimDeviceById(this.dSimpleId);
        verify(this.network).getDevice("device name");  // Not found in cache and then looked for it in network
    }
}

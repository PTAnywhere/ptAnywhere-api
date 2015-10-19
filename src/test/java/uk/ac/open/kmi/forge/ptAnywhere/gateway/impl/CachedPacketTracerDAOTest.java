package uk.ac.open.kmi.forge.ptAnywhere.gateway.impl;

import java.util.HashMap;
import java.util.Map;
import com.cisco.pt.ipc.sim.Router;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import com.cisco.pt.impl.UUIDImpl;
import com.cisco.pt.ipc.sim.Device;
import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.ui.LogicalWorkspace;
import org.mockito.InOrder;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.Utils;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.DeviceNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.gateway.Cache;


public class CachedPacketTracerDAOTest {

    final String netUuid = "{a9101f6b-ef7c-4372-91c2-9391e94ee233}";
    final String netSimpleId = "qRAfa.98Q3KRwpOR6U7iMw--";
    final String testedDeviceSimpleId = "d8d5exozT9eNsR1udGjbZQ--";
    final String testedDeviceName = "name1";

    Cache cache;
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
        this.tested.getSimDeviceById(this.testedDeviceSimpleId);
        verify(this.network).getDeviceCount();  // Not found in cache and then looked for it in network
    }

    @Test(expected = DeviceNotFoundException.class)
    public void testGetSimDeviceByIdCached() {
        this.cache.add(this.netSimpleId, this.testedDeviceSimpleId, this.testedDeviceName);
        this.tested.getSimDeviceById(this.testedDeviceSimpleId);
        verify(this.network).getDevice(this.testedDeviceName);  // Found in cache and then looked for it in network
    }

    @Test(expected = DeviceNotFoundException.class)
    public void testGetDeviceNameNotCached() {
        this.tested.getDeviceName(this.testedDeviceSimpleId);
        verify(this.network).getDeviceCount();  // Not found in cache and then looked for it in network
    }

    @Test
    public void testGetDeviceNameCached() {
        this.cache.add(this.netSimpleId, this.testedDeviceSimpleId, this.testedDeviceName);
        // If it has the name cached, the method returns it without checking the network
        assertEquals(this.testedDeviceName, this.tested.getDeviceName(this.testedDeviceSimpleId));
    }

    @Test(expected = DeviceNotFoundException.class)
    public void testGetSimDevicesByIdsNotCached() {
        this.tested.getSimDevicesByIds(this.testedDeviceSimpleId, "a8d5exozT9eNsR1udGjbZQ--");
        verify(this.network).getDeviceCount();  // Not found in cache and then looked for it in network
    }

    @Test(expected = DeviceNotFoundException.class)
    public void testGetSimDevicesByIdsOneCached() {
        this.cache.add(this.netSimpleId, this.testedDeviceSimpleId, this.testedDeviceName);
        this.tested.getSimDevicesByIds("a8d5exozT9eNsR1udGjbZQ--", this.testedDeviceSimpleId);
        verify(this.network).getDevice(this.testedDeviceName);  // Found in cache and then looked for it in network
    }

    protected Device createMockedDevice(String id, String name, int position) {
        final Device ret = mock(Router.class);
        when(ret.getObjectUUID()).thenReturn(Utils.toCiscoUUID(id));
        when(ret.getName()).thenReturn(name);
        when(ret.getXCoordinate()).thenReturn(0.0);
        when(ret.getYCoordinate()).thenReturn(0.0);
        when(this.network.getDeviceAt(position)).thenReturn(ret);
        when(this.network.getDevice(name)).thenReturn(ret);
        return ret;
    }

    protected Map<String, Device> addMockDevicesToNetwork() {
        final Map<String, Device> addedDevices = new HashMap<>();
        addedDevices.put("a8d5exozT9eNsR1udGjbZQ--", createMockedDevice("a8d5exozT9eNsR1udGjbZQ--", "name2", 0));
        addedDevices.put("b8d5exozT9eNsR1udGjbZQ--", createMockedDevice("b8d5exozT9eNsR1udGjbZQ--", "name3", 1));
        addedDevices.put(this.testedDeviceSimpleId, createMockedDevice(this.testedDeviceSimpleId, this.testedDeviceName, 2));
        addedDevices.put("c8d5exozT9eNsR1udGjbZQ--", createMockedDevice("c8d5exozT9eNsR1udGjbZQ--", "name4", 3));
        when(this.network.getDeviceCount()).thenReturn(addedDevices.size());
        return addedDevices;
    }

    protected void prepareCacheChecking() {
        this.cache = mock(Cache.class);
        this.tested = new CachedPacketTracerDAO(this.workspace, this.network, this.cache);
        addMockDevicesToNetwork();
    }

    @Test
    public void testCachingByGetSimDeviceById() {
        prepareCacheChecking();
        this.tested.getSimDeviceById(this.testedDeviceSimpleId);
        verify(this.cache).add(this.netSimpleId, this.testedDeviceSimpleId, this.testedDeviceName);
    }

    @Test
    public void testUnCachingByRemoveDevice() {
        prepareCacheChecking();
        this.tested.removeDevice(this.testedDeviceSimpleId);
        verify(this.cache).remove(this.netSimpleId, this.testedDeviceSimpleId);
    }

    @Test
    public void testCachingByModifyDevice() {
        prepareCacheChecking();
        this.tested.modifyDevice(new uk.ac.open.kmi.forge.ptAnywhere.pojo.Device(this.testedDeviceSimpleId, "new name", 0, 0, "routerDevice"));

        final InOrder inOrder = inOrder(this.cache);
        inOrder.verify(this.cache).remove(this.netSimpleId, this.testedDeviceSimpleId);
        // Ideally "new name" instead of anyString(), but as devices are mocks where set does not have effect...
        inOrder.verify(this.cache).add(eq(this.netSimpleId), eq(this.testedDeviceSimpleId), anyString());
    }
}
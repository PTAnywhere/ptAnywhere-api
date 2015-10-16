package uk.ac.open.kmi.forge.ptAnywhere.gateway.impl;

import com.cisco.pt.UUID;
import com.cisco.pt.impl.IPCMessageLTV;
import com.cisco.pt.ipc.IPCFactory;
import com.cisco.pt.ipc.sim.Device;
import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ptmp.PacketTracerSession;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.Utils;
import uk.ac.open.kmi.forge.ptAnywhere.gateway.Cache;


/**
 * Network interface wrapper which caches IDs and names.
 */
public class CachingNetwork implements Network {

    final Network network;
    final Cache cache;

    CachingNetwork(Network network, Cache cache) {
        this.network = network;
        this.cache = cache;
    }

    protected Device cacheDevice(Device device) {
        if (device!=null)
            this.cache.add( Utils.toSimplifiedId(getObjectUUID()),
                            Utils.toSimplifiedId(device.getObjectUUID()), device.getName());
        return device;
    }

    public Device getDevice(String name) {
        return cacheDevice(this.network.getDevice(name));
    }

    public Device getDeviceAt(int position) {
        return cacheDevice(this.network.getDeviceAt(position));
    }

    public int getDeviceCount() {
        return this.network.getDeviceCount();
    }

    public PacketTracerSession getPacketTracerSession() {
        return this.network.getPacketTracerSession();
    }

    public IPCFactory getFactory() {
        return this.network.getFactory();
    }

    public IPCMessageLTV getAccessMessage() {
        return this.network.getAccessMessage();
    }

    public String getClassName() {
        return this.network.getClassName();
    }

    public UUID getObjectUUID() {
        return this.network.getObjectUUID();
    }
}
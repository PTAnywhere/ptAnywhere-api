package uk.ac.open.kmi.forge.ptAnywhere.gateway.impl;

import com.cisco.pt.ipc.sim.Device;
import com.cisco.pt.ipc.ui.IPC;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.Utils;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.DeviceNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.gateway.Cache;
import java.util.*;


/**
 * PacketTracer DAO implementation which caches device names.
 *
 * For more info, visit: https://github.com/PTAnywhere/ptAnywhere-api/issues/9
 */
public class CachedPacketTracerDAO extends BasicPacketTracerDAO {

    final Cache cache;
    final String networkId;

    public CachedPacketTracerDAO(IPC ipc, Cache cache) {
        super(ipc, new CachingNetwork(ipc.network(), cache));
        this.networkId = Utils.toSimplifiedId(network.getObjectUUID());
        this.cache = cache;
    }

    @Override
    public com.cisco.pt.ipc.sim.Device getSimDeviceById(String simplifiedId) throws DeviceNotFoundException {
        final String name = this.cache.getName(this.networkId, simplifiedId);
        if (name==null) {  // Not in the cache
            return super.getSimDeviceById(simplifiedId);
        } else {
            return super.getSimDeviceByName(name);
        }
    }

    @Override
    protected Map<String, com.cisco.pt.ipc.sim.Device> getSimDevicesByIds(String... deviceIds) throws DeviceNotFoundException {
        final Map<String, Device> ret = new HashMap<String, Device>();
        final Set<String> toFindById = new HashSet<String>();
        for(String deviceId: deviceIds) {
            final String name = this.cache.getName(this.networkId, deviceId);
            if (name==null) {
                toFindById.add(deviceId);
            } else { // Device name in the cache
                // getSimDeviceByName might throw DeviceNotFoundException
                ret.put(deviceId, super.getSimDeviceByName(name) );
            }
        }
        if (ret.size()==deviceIds.length) return ret;
        return super.getSimDevicesByIds(ret, (String[]) toFindById.toArray());
    }

    /***************************** Cache Updates  *******************************/
    // createDevice => already caches it when it calls to getSimDeviceById

    @Override
    public uk.ac.open.kmi.forge.ptAnywhere.pojo.Device removeDevice(String deviceId) {
        final uk.ac.open.kmi.forge.ptAnywhere.pojo.Device ret = super.removeDevice(deviceId);
        this.cache.remove(this.networkId, deviceId);
        return ret;
    }

    @Override
    public uk.ac.open.kmi.forge.ptAnywhere.pojo.Device modifyDevice(uk.ac.open.kmi.forge.ptAnywhere.pojo.Device modification) {
        final uk.ac.open.kmi.forge.ptAnywhere.pojo.Device ret = super.modifyDevice(modification);
        this.cache.remove(this.networkId, modification.getId());
        this.cache.add(this.networkId, modification.getId(), ret.getLabel());
        return ret;
    }
}

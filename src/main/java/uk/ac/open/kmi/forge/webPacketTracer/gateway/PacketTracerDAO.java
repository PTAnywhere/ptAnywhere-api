package uk.ac.open.kmi.forge.webPacketTracer.gateway;

import com.cisco.pt.ipc.sim.Device;
import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.ui.IPC;

/**
 * Data access object for PacketTracer.
 */
public class PacketTracerDAO {

    final IPC ipc;

    public PacketTracerDAO(IPC ipc) {
        this.ipc = ipc;
    }

    public Device getDeviceById(String deviceId) {
        final Network network = this.ipc.network();
        for (int i=0; i<network.getDeviceCount(); i++) {
            final com.cisco.pt.ipc.sim.Device ret = network.getDeviceAt(i);
            if (deviceId.equals(ret.getObjectUUID().getDecoratedHexString())) {
                return ret;
            }
        }
        return null;
    }
}
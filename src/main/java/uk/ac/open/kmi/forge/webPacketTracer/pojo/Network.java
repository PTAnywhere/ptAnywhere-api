package uk.ac.open.kmi.forge.webPacketTracer.pojo;

import com.cisco.pt.ipc.sim.port.Link;

import java.util.*;

public class Network {
    Collection<Device> devices;
    Collection<Edge> edges;

    public Network() {
        this.devices = new HashSet<Device>();
        this.edges = null;
    }

    public static Network fromCiscoObject(com.cisco.pt.ipc.sim.Network network) {
        final Network ret = new Network();
        final Map<String, Edge> edges = new HashMap<String, Edge>();
        for (int i = 0; i < network.getDeviceCount(); i++) {
            final com.cisco.pt.ipc.sim.Device d = network.getDeviceAt(i);
            ret.devices.add(Device.fromCiscoObject(d));
            for (int j = 0; j < d.getPortCount(); j++) {
                final com.cisco.pt.ipc.sim.port.Port port = d.getPortAt(j);
                final Link currentLink = port.getLink();
                if (currentLink != null) {
                    final String linkId = currentLink.getObjectUUID().getDecoratedHexString();
                    final String devId = d.getObjectUUID().getDecoratedHexString();
                    if (edges.containsKey(linkId)) {
                        edges.get(linkId).setTo(devId);
                    } else {
                        edges.put(linkId, new Edge(linkId, devId, null));
                    }
                }
            }
        }
        ret.edges = edges.values();
        return ret;
    }

    public Collection<Device> getDevices() {
        return devices;
    }

    public void setDevices(Collection<Device> devices) {
        this.devices = devices;
    }

    public Collection<Edge> getEdges() {
        return edges;
    }

    public void setEdges(Collection<Edge> edges) {
        this.edges = edges;
    }
}

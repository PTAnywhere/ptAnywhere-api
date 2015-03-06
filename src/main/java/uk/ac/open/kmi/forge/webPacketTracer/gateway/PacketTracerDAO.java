package uk.ac.open.kmi.forge.webPacketTracer.gateway;


import com.cisco.pt.IPAddress;
import com.cisco.pt.impl.IPAddressImpl;
import com.cisco.pt.ipc.IPCConstants;
import com.cisco.pt.ipc.enums.ConnectType;
import com.cisco.pt.ipc.enums.DeviceType;
import com.cisco.pt.ipc.sim.*;
import com.cisco.pt.ipc.sim.port.HostPort;
import com.cisco.pt.ipc.ui.IPC;
import com.cisco.pt.ipc.ui.LogicalWorkspace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.*;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Network;

import java.util.*;


/**
 * Data access object for PacketTracer.
 */
public class PacketTracerDAO {

    private static final Log LOGGER = LogFactory.getLog(PacketTracerDAO.class);
    final IPC ipc;
    final com.cisco.pt.ipc.sim.Network network;  // It is used often, so better to put it as attribute.

    public PacketTracerDAO(IPC ipc) {
        this.ipc = ipc;
        this.network = this.ipc.network();
    }

    public Network getWholeNetwork() {
        final Network ret = new Network();
        final Map<String, Edge> edges = new HashMap<String, Edge>();
        for (int i = 0; i < this.network.getDeviceCount(); i++) {
            final com.cisco.pt.ipc.sim.Device d = this.network.getDeviceAt(i);
            ret.getDevices().add(Device.fromCiscoObject(d));
            for (int j = 0; j < d.getPortCount(); j++) {
                final com.cisco.pt.ipc.sim.port.Port port = d.getPortAt(j);
                final com.cisco.pt.ipc.sim.port.Link currentLink = port.getLink();
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
        ret.setEdges(edges.values());
        return ret;
    }

    public Set<Device> getDevices() {
        final Set<Device> ret = new HashSet<uk.ac.open.kmi.forge.webPacketTracer.pojo.Device>();
        for(int i = 0; i<this.network.getDeviceCount();i++) {
            ret.add(Device.fromCiscoObject(this.network.getDeviceAt(i)));
        }
        return ret;
    }

    private DeviceType getType(Device device) {
        final String g = device.getGroup();
        if (g.contains("switch")) {
            return DeviceType.SWITCH;
        } else if (g.contains("router")) {
            return DeviceType.ROUTER;
        } else if (g.contains("pc")) {
            return DeviceType.PC;
        } else if (g.contains("cloud")) {
            return DeviceType.CLOUD;
        }
        return null;
    }

    private String getDefaultModelName(DeviceType type) {
        switch(type) {
            case SWITCH: return IPCConstants.DEVICE_NAME_SWITCH_2960_24TT;
            case ROUTER: return IPCConstants.DEVICE_NAME_ROUTER_2620XM; // "2901";
            case PC: return IPCConstants.DEVICE_NAME_PC_PT;
            case CLOUD: return IPCConstants.DEVICE_NAME_CLOUD_PT;
            default: return null;
        }
    }

    public Device createDevice(Device device) {
        final DeviceType type = getType(device);
        if (type==null) return null;

        final LogicalWorkspace workspace = this.ipc.appWindow().getActiveWorkspace().getLogicalWorkspace();
        final String addedDeviceName = workspace.addDevice(type, getDefaultModelName(type));
        final com.cisco.pt.ipc.sim.Device deviceAdded = getSimDeviceByName(addedDeviceName);
        final Device ret = Device.fromCiscoObject(deviceAdded);
        if (device.getX()!=-1 && device.getY()!=-1) { // Bad luck if you choose -1 position :-P
            // After struggling a lot with this, I have discovered that moveToLocation does
            // not move it to the coordinate you pass:
            //      + 0, 0 moves to 0, 0
            //      + 100, 100 moves to 150, 150
            //      + 300, 300 moves to 450, 450
            final double magicFactor = 1.5;
            deviceAdded.moveToLocation((int) (device.getX()/magicFactor),
                    (int) (device.getY()/magicFactor) );
            ret.setX(device.getX());
            ret.setY(device.getY());
        }
        if (device.getLabel()!=null) {
            // Problem: setName() makes deviceAdded.getObjectUUID() return null
            //          and moveToLocation not to work :-S
            // Cause: I guess that the name is used as an identify a device in all the related IPC
            //        protocol communications and (at least) these methods use IPC.
            // That's why we set it at the end and without calling either
            //   a) getObjectUUID() (fromCiscoObject calls it) or
            //   b) moveToLocation
            // afterwards.
            deviceAdded.setName(device.getLabel());
            ret.setLabel(device.getLabel());
        }
        return ret;
    }

    public com.cisco.pt.ipc.sim.Device getSimDeviceById(String deviceId) {
        for (int i=0; i<this.network.getDeviceCount(); i++) {
            final com.cisco.pt.ipc.sim.Device ret = this.network.getDeviceAt(i);
            if (deviceId.equals(ret.getObjectUUID().getDecoratedHexString())) {
                return ret;
            }
        }
        return null;
    }

    protected com.cisco.pt.ipc.sim.Device getSimDeviceByName(String deviceName) {
        return this.network.getDevice(deviceName);
    }

    public Device getDeviceById(String deviceId) {
        return getDeviceById(deviceId, false);
    }

    public Device getDeviceById(String deviceId, boolean loadPorts) {
        final com.cisco.pt.ipc.sim.Device d = getSimDeviceById(deviceId);
        final Device ret = Device.fromCiscoObject(d);
        if(loadPorts) ret.setPorts(getPorts(d));
        return ret;
    }

    public Device getDeviceByName(String deviceName) {
        return getDeviceByName(deviceName, false);
    }

    public Device getDeviceByName(String deviceName, boolean loadPorts) {
        final com.cisco.pt.ipc.sim.Device d = getSimDeviceByName(deviceName);
        final Device ret = Device.fromCiscoObject(d);
        if(loadPorts) ret.setPorts(getPorts(d));
        return ret;
    }

    public Device removeDevice(String deviceId) {
        final Device ret = getDeviceById(deviceId);
        if (ret!=null) {
            final LogicalWorkspace workspace = this.ipc.appWindow().getActiveWorkspace().getLogicalWorkspace();
            workspace.removeDevice(ret.getLabel());  // It can only be removed by name :-S
        }
        return ret;
    }

    public Device modifyDevice(Device modification) {
        final com.cisco.pt.ipc.sim.Device ret = getSimDeviceById(modification.getId());
        if (ret!=null) {
            ret.setName(modification.getLabel());  // Right now, we only allow to change the name of the label!
        }
        return Device.fromCiscoObject(ret);
    }

    private List<Port> getPorts(com.cisco.pt.ipc.sim.Device device) {
        return getPorts(device, false);
    }

    private List<Port> getPorts(com.cisco.pt.ipc.sim.Device device, boolean filterFree) {
        final List<Port> ports = new ArrayList<Port>();
        for(int i=0; i<device.getPortCount(); i++) {
            com.cisco.pt.ipc.sim.port.Port port = device.getPortAt(i);
            if(!filterFree || port.getLink()==null) {
            // If not filter => adds it, if filter, depends if it does not have a link.
                ports.add(Port.fromCiscoObject(port));
            }
        }
        return ports;
    }

    public List<Port> getPorts(String deviceId, boolean filterFree) {
        return getPorts(deviceId, false, false);
    }

    public List<Port> getPorts(String deviceId, boolean byName, boolean filterFree) {
        if (byName)
            return getPorts(getSimDeviceByName(deviceId), filterFree);
        else
            return getPorts(getSimDeviceById(deviceId), filterFree);
    }

    protected com.cisco.pt.ipc.sim.port.Port getSimPort(com.cisco.pt.ipc.sim.Device device, String portName) {
        for (int i = 0; i < device.getPortCount(); i++) {
            final com.cisco.pt.ipc.sim.port.Port port = device.getPortAt(i);
            if (portName.equals(port.getName())) {
                return port;
            }
        }
        return null;
    }

    protected com.cisco.pt.ipc.sim.port.Port getSimPort(String deviceId, String portName) {
        return getSimPort(getSimDeviceById(deviceId), portName);
    }

    public Port getPort(String deviceId, String portName) {
        return Port.fromCiscoObject(getSimPort(deviceId, portName));
    }

    public Port modifyPort(String deviceId, Port modification) {
        final com.cisco.pt.ipc.sim.port.Port p = getSimPort(deviceId, modification.getPortName());
        if (p!=null) {
            if (p instanceof HostPort) {
                final IPAddress ip = new IPAddressImpl(modification.getPortIpAddress());
                final IPAddress subnet = new IPAddressImpl(modification.getPortSubnetMask());
                ((HostPort) p).setIpSubnetMask(ip, subnet);
            }
            return Port.fromCiscoObject(p);
        }
        return null;
    }

    public Link getLink(String deviceId, String portName) {
        final String linkId = getPort(deviceId, portName).getLink();

        if (linkId!=null) {
            final Link ret = new Link();
            ret.setId(linkId);
            for (int i = 0; i < this.network.getDeviceCount(); i++) {
                final com.cisco.pt.ipc.sim.Device d = this.network.getDeviceAt(i);
                final String dId = d.getObjectUUID().getDecoratedHexString();
                if (!deviceId.equals(dId))
                    for (int j = 0; j < d.getPortCount(); j++) {
                        final com.cisco.pt.ipc.sim.port.Port p = d.getPortAt(j);
                        final String lId = (p.getLink() == null) ? null : p.getLink().getObjectUUID().getDecoratedHexString();
                        if (lId != null && ret.getId().equals(lId)) {
                            ret.setToDevice(d.getName());
                            ret.setToPort(p.getName());
                            return ret;
                        }
                    }
            }
        }
        return null;
    }

    public boolean createLink(String fromDeviceId, String fromPortName, Link newLink) {
        final LogicalWorkspace workspace = this.ipc.appWindow().getActiveWorkspace().getLogicalWorkspace();
        final com.cisco.pt.ipc.sim.Device device = getSimDeviceById(fromDeviceId);
        if (device==null) return false;
        return workspace.createLink(device.getName(), fromPortName, newLink.getToDevice(), newLink.getToPort(), ConnectType.ETHERNET_STRAIGHT);
    }

    public boolean removeLink(String fromDeviceId, String fromPortName) {
        final LogicalWorkspace workspace = this.ipc.appWindow().getActiveWorkspace().getLogicalWorkspace();
        final com.cisco.pt.ipc.sim.Device device = getSimDeviceById(fromDeviceId);
        if (device==null) return false;
        return workspace.deleteLink(device.getName(), fromPortName);
    }
}
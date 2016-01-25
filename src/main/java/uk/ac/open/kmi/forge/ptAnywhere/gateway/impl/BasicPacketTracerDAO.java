package uk.ac.open.kmi.forge.ptAnywhere.gateway.impl;

import java.util.*;
import com.cisco.pt.*;
import com.cisco.pt.UUID;
import com.cisco.pt.impl.IPAddressImpl;
import com.cisco.pt.ipc.IPCConstants;
import com.cisco.pt.ipc.enums.ConnectType;
import com.cisco.pt.ipc.enums.DeviceType;
import com.cisco.pt.ipc.sim.Pc;
import com.cisco.pt.ipc.sim.port.HostPort;
import com.cisco.pt.ipc.ui.IPC;
import com.cisco.pt.ipc.ui.LogicalWorkspace;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.URLFactory;
import uk.ac.open.kmi.forge.ptAnywhere.api.http.Utils;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.DeviceNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.LinkNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.PortNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.gateway.PacketTracerDAO;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.*;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.Device;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.Network;


/**
 * Basic Data access object for PacketTracer.
 */
public class BasicPacketTracerDAO implements PacketTracerDAO {

    private static final Log LOGGER = LogFactory.getLog(BasicPacketTracerDAO.class);
    final LogicalWorkspace workspace;
    final com.cisco.pt.ipc.sim.Network network;  // It is used often, so better to put it as attribute.


    public BasicPacketTracerDAO(IPC ipc) {
        this(ipc.appWindow().getActiveWorkspace().getLogicalWorkspace(), ipc.network());
    }

    protected BasicPacketTracerDAO(LogicalWorkspace workspace, com.cisco.pt.ipc.sim.Network network) {
        this.workspace = workspace;
        this.network = network;
    }

    @Override
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
                    final String linkId = Utils.toSimplifiedId(currentLink.getObjectUUID());
                    final String devId = Utils.toSimplifiedId(d.getObjectUUID());
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

    @Override
    public Set<Device> getDevices() {
        final Set<Device> ret = new HashSet<uk.ac.open.kmi.forge.ptAnywhere.pojo.Device>();
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

    @Override
    public Device createDevice(Device device) {
        final DeviceType type = getType(device);
        if (type==null) return null;

        final String addedDeviceName = this.workspace.addDevice(type, getDefaultModelName(type));
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
            // Problem: https://github.com/PTAnywhere/ptAnywhere-api/issues/25
            // Therefore, we should not use "deviceAdded" after calling "setName".
            deviceAdded.setName(device.getLabel());
            ret.setLabel(device.getLabel());
        }
        return ret;
    }

    @Override
    public com.cisco.pt.ipc.sim.Device getSimDeviceById(String simplifiedId) throws DeviceNotFoundException {
        return getSimDeviceByCiscoId(Utils.toCiscoUUID(simplifiedId));
    }

    @Override
    public String getDeviceName(String simplifiedId) {
        final com.cisco.pt.ipc.sim.Device d = getSimDeviceById(simplifiedId);
        if (d == null) return null;
        return d.getName();
    }

    protected com.cisco.pt.ipc.sim.Device getSimDeviceByCiscoId(UUID deviceId) throws DeviceNotFoundException {
        for (int i=0; i<this.network.getDeviceCount(); i++) {
            final com.cisco.pt.ipc.sim.Device ret = this.network.getDeviceAt(i);
            if (deviceId.equals(ret.getObjectUUID())) {
                return ret;
            }
        }
        throw new DeviceNotFoundException(deviceId.toString());
    }

    protected Map<String, com.cisco.pt.ipc.sim.Device> getSimDevicesByIds(Map<String, com.cisco.pt.ipc.sim.Device> ret, String... deviceIds) throws DeviceNotFoundException {
        for (int i=0; i<this.network.getDeviceCount(); i++) {
            final com.cisco.pt.ipc.sim.Device d = this.network.getDeviceAt(i);
            final String dSimplifiedId = Utils.toSimplifiedId(d.getObjectUUID());
            if (ArrayUtils.contains(deviceIds, dSimplifiedId)) {
                ret.put(dSimplifiedId, d);
                if (ret.size()==deviceIds.length) return ret;
            }
        }
        throw new DeviceNotFoundException(ArrayUtils.toString(deviceIds));
    }

    protected Map<String, com.cisco.pt.ipc.sim.Device> getSimDevicesByIds(String... deviceIds) throws DeviceNotFoundException {
        return this.getSimDevicesByIds(new HashMap<String, com.cisco.pt.ipc.sim.Device>(), deviceIds);
    }

    protected com.cisco.pt.ipc.sim.Device getSimDeviceByName(String deviceName) throws DeviceNotFoundException {
        final com.cisco.pt.ipc.sim.Device ret = this.network.getDevice(deviceName);
        if (ret!=null) return ret;
        throw new DeviceNotFoundException(deviceName);
    }

    @Override
    public Device getDeviceById(String deviceId) {
        return getDeviceById(deviceId, false);
    }

    @Override
    public Device getDeviceById(String deviceId, boolean loadPorts) {
        final com.cisco.pt.ipc.sim.Device d = getSimDeviceById(deviceId);
        final Device ret = Device.fromCiscoObject(d);
        if(loadPorts) ret.setPorts(getPorts(d));
        return ret;
    }

    @Override
    public Device getDeviceByName(String deviceName) {
        return getDeviceByName(deviceName, false);
    }

    @Override
    public Device getDeviceByName(String deviceName, boolean loadPorts) {
        final com.cisco.pt.ipc.sim.Device d = getSimDeviceByName(deviceName);
        final Device ret = Device.fromCiscoObject(d);
        if(loadPorts) ret.setPorts(getPorts(d));
        return ret;
    }

    @Override
    public Device removeDevice(String deviceId) {
        final Device ret = getDeviceById(deviceId);
        if (ret!=null) {
            this.workspace.removeDevice(ret.getLabel());  // It can only be removed by name :-S
        }
        return ret;
    }

    @Override
    public Device modifyDevice(Device modification) {
        Device ret = null;
        final com.cisco.pt.ipc.sim.Device dev = getSimDeviceById(modification.getId());
        if (dev!=null) {
            // TODO distinguish between devices in a more elegant way
            if (ret instanceof Pc && modification.getDefaultGateway()!=null) {
                final IPAddress gateway = new IPAddressImpl(modification.getDefaultGateway());
                ((Pc) dev).setDefaultGateway(gateway);
            }
            ret = Device.fromCiscoObject(dev, modification.getDefaultGateway());
            ret.setLabel(modification.getLabel());

            // Problem: https://github.com/PTAnywhere/ptAnywhere-api/issues/25
            // Therefore, we should not use "dev" after calling "setName".
            dev.setName(modification.getLabel());
        }
        return ret;
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

    @Override
    public List<Port> getPorts(String deviceId, boolean filterFree) {
        return getPorts(deviceId, false, false);
    }

    @Override
    public List<Port> getPorts(String deviceId, boolean byName, boolean filterFree) {
        if (byName)
            return getPorts(getSimDeviceByName(deviceId), filterFree);
        else
            return getPorts(getSimDeviceById(deviceId), filterFree);
    }

    protected com.cisco.pt.ipc.sim.port.Port getSimPort(com.cisco.pt.ipc.sim.Device device, String portName) throws PortNotFoundException {
        for (int i = 0; i < device.getPortCount(); i++) {
            final com.cisco.pt.ipc.sim.port.Port port = device.getPortAt(i);
            if (portName.equals(port.getName())) {
                return port;
            }
        }
        throw new PortNotFoundException(device.getName(), portName);
    }

    protected com.cisco.pt.ipc.sim.port.Port getSimPort(String deviceId, String portName) {
        return getSimPort(getSimDeviceById(deviceId), portName);
    }

    @Override
    public Port getPort(String deviceId, String portName) {
        return Port.fromCiscoObject(getSimPort(deviceId, portName));
    }

    @Override
    public Port modifyPort(String deviceId, Port modification) {
        final com.cisco.pt.ipc.sim.port.Port p = getSimPort(deviceId, modification.getPortName());
        // p!=null because otherwise the previous method would have thrown a PortNotFoundException.
        if (p instanceof HostPort) {
            final IPAddress ip = new IPAddressImpl(modification.getPortIpAddress());
            final IPAddress subnet = new IPAddressImpl(modification.getPortSubnetMask());
            ((HostPort) p).setIpSubnetMask(ip, subnet);
        }
        return Port.fromCiscoObject(p);
    }

    protected InnerLink getLink(InnerLink link) throws LinkNotFoundException {
        for (int i = 0; i < this.network.getDeviceCount(); i++) {
            final com.cisco.pt.ipc.sim.Device d = this.network.getDeviceAt(i);
            for (int j = 0; j < d.getPortCount(); j++) {
                final com.cisco.pt.ipc.sim.port.Port p = d.getPortAt(j);
                final String lId = (p.getLink() == null)? null : Utils.toSimplifiedId(p.getLink().getObjectUUID());
                if (link.getId().equals(lId)) {
                    link.appendEndpoint(Utils.toSimplifiedId(d.getObjectUUID()), p.getName());
                    if (link.areEndpointsSet())
                        return link;
                        // Check in the next device (I am assuming that a device cannot be connected to itself!)
                    else break;  // Keep finding the other endpoint in the next device
                }
            }
        }
        throw new LinkNotFoundException(link.getId());
    }

    @Override
    public InnerLink getLink(String linkId) throws LinkNotFoundException {
        if (linkId!=null) {
            final InnerLink link = new InnerLink(linkId);
            return getLink(link);
        }
        throw new LinkNotFoundException(linkId);
    }

    @Override
    public InnerLink getLink(String deviceId, String portName) throws LinkNotFoundException {
        final String linkId = getPort(deviceId, portName).getLink();
        if (linkId!=null) {
            final InnerLink link = new InnerLink(linkId);
            link.appendEndpoint(deviceId, portName);
            return getLink(link);
        }
        throw new LinkNotFoundException(deviceId, portName);
    }

    @Override
    public boolean createLink(String fromDeviceId, String fromPortName, HalfLink newLink) {
        final String toDeviceId = URLFactory.parseDeviceId(newLink.getToPort());
        final Map<String, com.cisco.pt.ipc.sim.Device> devices = getSimDevicesByIds(fromDeviceId, toDeviceId);

        final com.cisco.pt.ipc.sim.Device fromDevice = devices.get(fromDeviceId);
        final com.cisco.pt.ipc.sim.Device toDevice = devices.get(toDeviceId);

        final String toPortName = URLFactory.parsePortId(newLink.getToPort());
        return this.workspace.createLink(fromDevice.getName(), fromPortName, toDevice.getName(), toPortName, ConnectType.ETHERNET_STRAIGHT);
    }

    @Override
    public boolean removeLink(String fromDeviceId, String fromPortName) {
        final com.cisco.pt.ipc.sim.Device device = getSimDeviceById(fromDeviceId);
        if (device==null) return false;
        return this.workspace.deleteLink(device.getName(), fromPortName);
    }
}
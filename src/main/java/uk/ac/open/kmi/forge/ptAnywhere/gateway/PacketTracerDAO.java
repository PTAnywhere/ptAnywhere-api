package uk.ac.open.kmi.forge.ptAnywhere.gateway;

import uk.ac.open.kmi.forge.ptAnywhere.exceptions.DeviceNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.LinkNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.*;

import java.util.List;
import java.util.Set;

/**
 * Created by agg96 on 10/16/15.
 */
public interface PacketTracerDAO {
    Network getWholeNetwork();

    Set<Device> getDevices();

    Device createDevice(Device device);

    com.cisco.pt.ipc.sim.Device getSimDeviceById(String simplifiedId) throws DeviceNotFoundException;

    Device getDeviceById(String deviceId);

    Device getDeviceById(String deviceId, boolean loadPorts);

    Device getDeviceByName(String deviceName);

    Device getDeviceByName(String deviceName, boolean loadPorts);

    Device removeDevice(String deviceId);

    Device modifyDevice(Device modification);

    List<Port> getPorts(String deviceId, boolean filterFree);

    List<Port> getPorts(String deviceId, boolean byName, boolean filterFree);

    Port getPort(String deviceId, String portName);

    Port modifyPort(String deviceId, Port modification);

    InnerLink getLink(String linkId) throws LinkNotFoundException;

    InnerLink getLink(String deviceId, String portName) throws LinkNotFoundException;

    boolean createLink(String fromDeviceId, String fromPortName, HalfLink newLink);

    boolean removeLink(String fromDeviceId, String fromPortName);
}

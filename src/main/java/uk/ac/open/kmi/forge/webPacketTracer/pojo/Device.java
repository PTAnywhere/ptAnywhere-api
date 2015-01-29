package uk.ac.open.kmi.forge.webPacketTracer.pojo;

import com.cisco.pt.ipc.sim.Cloud;
import com.cisco.pt.ipc.sim.Pc;
import com.cisco.pt.ipc.sim.Router;
import com.cisco.pt.ipc.sim.port.HostPort;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Device {
    String id;  // E.g., "{4e70e5d7-4399-485e-b409-6c9d1c9446ea}"
    String label;  // E.g., "MySwitch"
    int x;  // E.g., 436
    int y; // E.g.,
    String group; // E.g., "switchDevice"
    List<Port> ports;

    public Device() {
    }

    public Device(String id, String label, int x, int y, String group) {
        this.id = id;
        this.label = label;
        this.x = x;
        this.y = y;
        this.group = group;
    }

    public static Device fromCiscoObject(com.cisco.pt.ipc.sim.Device device) {
        return fromCiscoObject(device, true);
    }

    public static Device fromCiscoObject(com.cisco.pt.ipc.sim.Device device, boolean loadPorts) {
        final String id = device.getObjectUUID().getDecoratedHexString();
        final String label = /*device.getClass() + ":" + device.getModel()
                               + ":" + */device.getName();
        final int deviceX = (int) (device.getXCoordinate()*1.5);
        final int deviceY = (int) (device.getYCoordinate()*1.5);
        final String group;
        if (device instanceof Router) {
            group = "routerDevice";
        } else if (device instanceof Cloud) {
            group = "cloudDevice";
        } else if (device instanceof Pc) {
            group = "pcDevice";
        } else {
            group = "switchDevice";
        }
        final Device ret = new Device(id, label, deviceX, deviceY, group);
        if (loadPorts) {
            final List<Port> ports = new ArrayList<Port>();
            for(int i=0; i<device.getPortCount(); i++) {
                com.cisco.pt.ipc.sim.port.Port port = device.getPortAt(i);
                ports.add(Port.fromCiscoObject(port));
            }
            ret.setPorts(ports);
        }
        return ret;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<Port> getPorts() {
        return ports;
    }

    public void setPorts(List<Port> ports) {
        this.ports = ports;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Device)) return false;

        Device device = (Device) o;

        if (!id.equals(device.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Device{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", group='" + group + '\'' +
                ", ports=" + ports +
                '}';
    }
}

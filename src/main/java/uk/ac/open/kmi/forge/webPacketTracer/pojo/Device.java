package uk.ac.open.kmi.forge.webPacketTracer.pojo;

import com.cisco.pt.ipc.sim.Cloud;
import com.cisco.pt.ipc.sim.Pc;
import com.cisco.pt.ipc.sim.Router;
import uk.ac.open.kmi.forge.webPacketTracer.api.http.Utils;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement
public class Device {
    String id;  // E.g., "b8d5exozT9eNsR1udGjbZQ--"
    String label;  // E.g., "MySwitch"
    double x;  // E.g., 436
    double y; // E.g., 345
    String group; // E.g., "switchDevice"
    List<Port> ports;

    public Device() {
        this(null, null, -1, -1, null);
    }

    public Device(String id, String label, int x, int y, String group) {
        this.id = id;
        this.label = label;
        this.x = x;
        this.y = y;
        this.group = group;
    }

    public static Device fromCiscoObject(com.cisco.pt.ipc.sim.Device device) {
        if (device==null) return null;
        final String id = Utils.toSimplifiedId(device.getObjectUUID());
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
        return new Device(id, label, deviceX, deviceY, group);
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

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
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

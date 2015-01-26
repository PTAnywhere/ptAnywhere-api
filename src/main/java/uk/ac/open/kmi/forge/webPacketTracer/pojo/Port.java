package uk.ac.open.kmi.forge.webPacketTracer.pojo;

import com.cisco.pt.ipc.sim.Cloud;
import com.cisco.pt.ipc.sim.Pc;
import com.cisco.pt.ipc.sim.Router;
import com.cisco.pt.ipc.sim.port.HostPort;
import com.cisco.pt.ipc.sim.port.Link;

// { "portName": "Vlan1", "portIpAddress": "0.0.0.0","portSubnetMask": "0.0.0.0"}
public class Port {

    String portName;  // E.g., "Vlan1"
    String portIpAddress;  // E.g., "0.0.0.0"
    String portSubnetMask;  // E.g., "0.0.0.0"
    String linkId;

    public Port() {
    }

    public Port(String portName, String portIpAddress, String portSubnetMask, String linkId) {
        this.portName = portName;
        this.portIpAddress = portIpAddress;
        this.portSubnetMask = portSubnetMask;
        this.linkId = linkId;
    }

    public static Port fromCiscoObject(com.cisco.pt.ipc.sim.port.HostPort port) {
        final Link l = port.getLink();
        return new Port( port.getName(),
                         port.getIpAddress().getDottedQuadString(),
                         port.getSubnetMask().getDottedQuadString(),
                         (l==null)? null: l.getObjectUUID().getDecoratedHexString() );
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public String getPortIpAddress() {
        return portIpAddress;
    }

    public void setPortIpAddress(String portIpAddress) {
        this.portIpAddress = portIpAddress;
    }

    public String getPortSubnetMask() {
        return portSubnetMask;
    }

    public void setPortSubnetMask(String portSubnetMask) {
        this.portSubnetMask = portSubnetMask;
    }

    public String getLink() {
        return linkId;
    }

    public void setLink(String linkId) {
        this.linkId = linkId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Port)) return false;

        Port port = (Port) o;

        if (portIpAddress != null ? !portIpAddress.equals(port.portIpAddress) : port.portIpAddress != null)
            return false;
        if (portName != null ? !portName.equals(port.portName) : port.portName != null) return false;
        if (portSubnetMask != null ? !portSubnetMask.equals(port.portSubnetMask) : port.portSubnetMask != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = portName != null ? portName.hashCode() : 0;
        result = 31 * result + (portIpAddress != null ? portIpAddress.hashCode() : 0);
        result = 31 * result + (portSubnetMask != null ? portSubnetMask.hashCode() : 0);
        return result;
    }
}

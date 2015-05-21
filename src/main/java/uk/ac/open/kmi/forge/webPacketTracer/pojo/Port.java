package uk.ac.open.kmi.forge.webPacketTracer.pojo;

import com.cisco.pt.ipc.sim.port.HostPort;
import com.cisco.pt.ipc.sim.port.Link;
import uk.ac.open.kmi.forge.webPacketTracer.api.http.Utils;

// { "portName": "Vlan1", "portIpAddress": "0.0.0.0","portSubnetMask": "0.0.0.0"}
public class Port {

    String portName;  // E.g., "Vlan1"
    String portIpAddress;  // E.g., "0.0.0.0"
    String portSubnetMask;  // E.g., "0.0.0.0"
    String linkId; // E.g., cc57bc49d73a42a5aa6a1c78066d565c

    public Port() {
    }

    public Port(String portName, String portIpAddress, String portSubnetMask, String linkId) {
        this.portName = portName;
        this.portIpAddress = portIpAddress;
        this.portSubnetMask = portSubnetMask;
        this.linkId = linkId;
    }

    public static Port fromCiscoObject(com.cisco.pt.ipc.sim.port.Port port) {
        if (port==null) return null;

        final Link l = port.getLink();
        final Port ret = new Port( port.getName(), "", "",
                (l==null)? null: Utils.toSimplifiedUUID(l.getObjectUUID().getDecoratedHexString()));
        if (port instanceof HostPort) {
            final HostPort hPort = (HostPort) port;
            ret.setPortIpAddress(hPort.getIpAddress().getDottedQuadString());
            ret.setPortSubnetMask(hPort.getSubnetMask().getDottedQuadString());
        }
        // It can also be a SwitchPort
        // getLog().error("Port " + port.getName() +
        //        " is not an instance of HostPort " + port.getType().toString());
        return ret;
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

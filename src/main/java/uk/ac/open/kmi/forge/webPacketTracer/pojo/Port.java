package uk.ac.open.kmi.forge.webPacketTracer.pojo;

// { "portName": "Vlan1", "portIpAddress": "0.0.0.0","portSubnetMask": "0.0.0.0"}
public class Port {
    String portName;  // E.g., "Vlan1"
    String portIpAddress;  // E.g., "0.0.0.0"
    String portSubnetMask;  // E.g., "0.0.0.0"

    public Port() {
    }

    public Port(String portName, String portIpAddress, String portSubnetMask) {
        this.portName = portName;
        this.portIpAddress = portIpAddress;
        this.portSubnetMask = portSubnetMask;
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

package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.IPAddress;
import com.cisco.pt.UUID;
import com.cisco.pt.backpacks.framework.Backpack;
import com.cisco.pt.impl.IPAddressImpl;
import com.cisco.pt.ipc.IPCConstants;
import com.cisco.pt.ipc.enums.CommandStatus;
import com.cisco.pt.ipc.enums.ConnectType;
import com.cisco.pt.ipc.enums.DeviceType;
import com.cisco.pt.ipc.sim.CiscoDevice;
import com.cisco.pt.ipc.sim.Cloud;
import com.cisco.pt.ipc.sim.Device;
import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.sim.Pc;
import com.cisco.pt.ipc.sim.Router;
import com.cisco.pt.ipc.sim.port.HostPort;
import com.cisco.pt.ipc.sim.port.Link;
import com.cisco.pt.ipc.sim.port.Port;
import com.cisco.pt.ipc.sim.port.RouterPort;
import com.cisco.pt.ipc.sim.port.impl.LinkImpl;
import com.cisco.pt.ipc.sim.process.Process;
import com.cisco.pt.ipc.ui.IPC;
import com.cisco.pt.ipc.ui.LogicalWorkspace;
import com.cisco.pt.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PtSmith extends Backpack {

	private String devicesJson = "";
	private String edgesJson = "";
	private Set<Device> devices;
	String deviceId, ipAddress, subnetMask,  defaultGateway, interfaceName;
	String newDeviceName, newDeviceType, deleteDeviceId;
	String linkSource, linkSourceInterface, linkTarget, linkTargetInterface;
	String deleteLinkDevice, deleteLinkInterface;

	private LogicalWorkspace workspace;

	public String getDevicesJson() {
		return devicesJson;
	}

	public String getEdgesJson() {
		return edgesJson;
	}

	public static void main(String[] args) throws Exception {
		Backpack backpack = new PtSmith();
		backpack.run();
	}

	@Override
	protected void internalRun() throws Exception {
		workspace = ipcFactory.getIPC().appWindow().getActiveWorkspace().getLogicalWorkspace();
		Set<Device> allDevices = getDevices();
		devices = allDevices;
		System.err.println("internalRun");
		if (newDeviceName != null && !newDeviceName.equals("")) {
			System.err.println("internalRun add");
			addDevice();
			System.err.println("internalRun add done");
		} else if (deleteDeviceId != null && !deleteDeviceId.equals("")) {
			System.err.println("internalRun delete");
			deleteDevice();
			System.err.println("internalRun delete done");
		} else if (linkSource != null && !linkSource.equals("") &&
				linkSourceInterface != null && !linkSourceInterface.equals("") &&
				linkTarget != null && !linkTarget.equals("") &&
				linkTargetInterface != null && !linkTargetInterface.equals("")) {
			System.err.println("internalRun createlink");
			boolean result = workspace.createLink(linkSource, linkSourceInterface, linkTarget, linkTargetInterface, ConnectType.AUTO);
			System.err.println("createlink: " + result);
			System.err.println("internalRun createlink done");
		} else if (deleteLinkDevice != null && !deleteLinkDevice.equals("") &&
				deleteLinkInterface != null && !deleteLinkInterface.equals("")) {
			System.err.println("internalRun deletelink");
			boolean result = workspace.deleteLink(deleteLinkDevice, deleteLinkInterface);
			System.err.println("deletelink: " + result);
			System.err.println("internalRun deletelink done");
		} else {
			System.err.println("internalRun config");
			configureDevice();
			System.err.println("internalRun config done");
			//if (devicesJson == null || devicesJson.equals("")){
			System.err.println("internalRun devices");
			devicesJson = devicesToJson(allDevices);
			System.err.println("internalRun devices done");
			//}
			//if (edgesJson == null || edgesJson.equals("")) {
			System.err.println("internalRun edges");
			edgesJson = getEdgesJson(allDevices);
			System.err.println("internalRun edges done");
			//	}

		}
	}

	public boolean addDevice() {
		if (newDeviceName != null && newDeviceType != null) {
			System.err.println("Adding device " + newDeviceName + " of type " + newDeviceType);

			/*model strings:
			 * Found 5 devices.
			 ***********Device model: 2960-24TT
			 ***********Device model: Cloud-PT
			 ***********Device model: PC-PT
			 ***********Device model: PC-PT
			 ***********Device model: 2901
			 */
			String modelString = "";
			DeviceType deviceType = null;
			if (newDeviceType.contains("switch")) {
				modelString = "2960-24TT";
				deviceType = DeviceType.SWITCH;
			} else if (newDeviceType.contains("router")) {
				modelString = "2901";
				deviceType = DeviceType.ROUTER;
			} else if (newDeviceType.contains("pc")) {
				modelString = "PC-PT";
				deviceType = DeviceType.PC;
			} else if (newDeviceType.contains("cloud")) {
				modelString = "Cloud-PT";
				deviceType = DeviceType.CLOUD;
			}

			String deviceAddedName = workspace.addDevice(deviceType, modelString);

			System.err.println("deviceAddedString " + deviceAddedName);

			Network network = ipcFactory.getIPC().network();

			Device deviceAdded = network.getDevice(deviceAddedName);
			deviceAdded.setName(newDeviceName);
		}
		return true;

	}

	public boolean deleteDevice() {
		if (deleteDeviceId != null && !deleteDeviceId.equals("")) {
			System.err.println("Going to delete device " + deleteDeviceId);
			String deviceName = "";
			for (Device device : devices) {
				System.err.println(device.getObjectUUID().getDecoratedHexString());
				if (device.getObjectUUID().getDecoratedHexString().equals(deleteDeviceId)) {
					deviceName = device.getName();
				}
			}
			if (!deviceName.equals("")) {
				workspace.removeDevice(deviceName);
			}
		}
		return true;
	}

	public boolean configureDevice() {
		System.err.println("A");
		if (ipAddress != null && subnetMask != null && defaultGateway != null 
				&& deviceId != null && interfaceName != null) {
			System.err.println("B");
			for (Device device : devices ) {
				System.err.println("C");
				if (device.getObjectUUID().getDecoratedHexString().contains(deviceId) ||
						deviceId.contains(device.getObjectUUID().getDecoratedHexString())) {
					System.err.println("D:" + device.getObjectUUID().getDecoratedHexString() + " " + deviceId);
					for (int j = 0; j < device.getPortCount(); j++) {
						System.err.println("H");
						Port port = device.getPortAt(j);
						System.err.println("Port name " + j + ": " + port.getName());
						try {
							if (port instanceof HostPort) {
								HostPort hostPort = (HostPort) port;

								System.err.println("IP address " + j + ": " + hostPort.getIpAddress().getDottedQuadString());
								if (port.getName().equals(interfaceName) 
										|| port.getName().contains(interfaceName)
										|| interfaceName.contains(port.getName())) {
									IPAddress ip = new IPAddressImpl(ipAddress);
									IPAddress subnet = new IPAddressImpl(subnetMask);
									System.err.println("Setting " + port.getName() + " to have IP address " +
											ip.getDottedQuadString() + " and subnet mask " + subnet.getDottedQuadString());
									hostPort.setIpSubnetMask(ip, subnet);
								}
							} else {
								System.err.println("Port " + port.getName() + 
										" is not an instance of HostPort " + port.getType().toString());
							}
						} catch (Throwable t) {
							System.err.println(t.getMessage());
						}
					}
					/*if (device instanceof CiscoDevice) {
						System.err.println(deviceId + " is an instance of Cisco device");
						CiscoDevice ciscoDevice = (CiscoDevice) device;
						String enableCommand = "enable";
						String configCommand = "config terminal";
						String interfaceCommand = "interface " + interfaceName;
						String setIPCommand = "ip address " + ipAddress + " " + subnetMask;
						String endCommand = "end";
						String[] executeCommands = {enableCommand, configCommand, interfaceCommand, setIPCommand, endCommand};
						System.err.println("E");
						for (int i = 0; i < device.getCustomVarsCount(); i++) {
							System.err.println("F");
							System.err.print("Custom var: ");
							System.err.print(device.getCustomVarNameAt(i) + ", ");
							System.err.println(device.getCustomVarValueStrAt(i));
						}
						System.err.println("G");
						for (int j = 0; j < device.getPortCount(); j++) {
							System.err.println("H");
							Port port = device.getPortAt(j);
							System.err.println("Port name " + j + ": " + port.getName());
							try {
								if (port instanceof HostPort) {
									HostPort hostPort = (HostPort) port;

									System.err.println("IP address " + j + ": " + hostPort.getIpAddress().getDottedQuadString());
									if (port.getName().equals(interfaceName)) {
										IPAddress ip = new IPAddressImpl(ipAddress);
										IPAddress subnet = new IPAddressImpl(subnetMask);
										hostPort.setIpSubnetMask(ip, subnet);
									}
								} else {
									System.err.println("Port " + port.getName() + 
											" is not an instance of HostPort " + port.getType().toString());
								}
							} catch (Throwable t) {
								System.err.println(t.getMessage());
							}
						}
						for (String command : executeCommands) {
							Pair<CommandStatus, String> answer = null;
							answer = ciscoDevice.enterCommand(setIPCommand, "");
							System.err.println("I: " + command);
							if (answer != null) {
								System.err.println(answer.getFirst());
								System.err.println(answer.getSecond());
							}
						}
					} else {
						System.err.println(deviceId + " is not an instance of Cisco device");
					}
					 */
					System.err.println("J");
					return true;
				}
			}
			System.err.println("K");
			return true;
		} else {
			System.err.println("L");
			return false;
		}
	}

	public boolean sendPing() {
		return true;
	}

	protected Set<Device> getDevices() {
		try {
			System.err.println("in getDevices");
			Set<Device> allDevices = new HashSet<Device>();

			Network network = ipcFactory.getIPC().network();

			System.err.println("Fetched network");
			int numDevices = network.getDeviceCount();
			System.err.println("Found " + numDevices + " devices.");
			for (int i = 0; i < numDevices; i++) {
				Device current = network.getDeviceAt(i);
				allDevices.add(current);
			}
			return allDevices;

		} catch (Throwable t) {
			if (t instanceof ThreadDeath) {
				throw (ThreadDeath) t;
			}

			t.printStackTrace();
		}
		return null;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getSubnetMask() {
		return subnetMask;
	}

	public void setSubnetMask(String subnetMask) {
		this.subnetMask = subnetMask;
	}

	public String getDefaultGateway() {
		return defaultGateway;
	}

	public void setDefaultGateway(String defaultGateway) {
		this.defaultGateway = defaultGateway;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getNewDeviceName() {
		return newDeviceName;
	}

	public void setNewDeviceName(String newDeviceName) {
		this.newDeviceName = newDeviceName;
	}

	public String getNewDeviceType() {
		return newDeviceType;
	}

	public void setNewDeviceType(String newDeviceType) {
		this.newDeviceType = newDeviceType;
	}

	public String getDeleteDeviceId() {
		return deleteDeviceId;
	}

	public void setDeleteDeviceId(String deleteDeviceId) {
		this.deleteDeviceId = deleteDeviceId;
	}

	public String getLinkSource() {
		return linkSource;
	}

	public void setLinkSource(String linkSource) {
		System.err.println(linkSource);
		this.linkSource = linkSource;
	}

	public String getLinkSourceInterface() {
		return linkSourceInterface;
	}

	public void setLinkSourceInterface(String linkSourceInterface) {
		System.err.println(linkSourceInterface);
		this.linkSourceInterface = linkSourceInterface;
	}

	public String getLinkTarget() {
		return linkTarget;
	}

	public void setLinkTarget(String linkTarget) {
		System.err.println(linkTarget);
		this.linkTarget = linkTarget;
	}

	public String getLinkTargetInterface() {
		return linkTargetInterface;
	}

	public void setLinkTargetInterface(String linkTargetInterface) {
		System.err.println(linkTargetInterface);
		this.linkTargetInterface = linkTargetInterface;
	}

	public String getDeleteLinkDevice() {
		return deleteLinkDevice;
	}

	public void setDeleteLinkDevice(String deleteLinkDevice) {
		this.deleteLinkDevice = deleteLinkDevice;
	}

	public String getDeleteLinkInterface() {
		return deleteLinkInterface;
	}

	public void setDeleteLinkInterface(String deleteLinkInterface) {
		this.deleteLinkInterface = deleteLinkInterface;
	}

	protected void dumpDevice(Device device) throws Exception {
		System.out.println("type = " + device.getType());
		System.out.println("class = " + device.getClass().getName());
		System.out.println("getClassName() = " + device.getClassName());
		System.out.println("getObjectUUID() = " + device.getObjectUUID());

	}

	protected String devicesToJson(Set<Device> devices) {
		String json = "[ \n";
		for (Device device : devices) {
			//System.err.println("***********Device model: " + device.getModel());
			String id = device.getObjectUUID().getDecoratedHexString();
			String label = /*device.getClass() + ":" + device.getModel() 
					+ ":" + */device.getName();
			json += " {\n \t\"id\": \"" + id + "\",\n\t\"label\": \"" + label + "\",\n ";
			int deviceX = (int) (device.getXCoordinate()*1.5);
			int deviceY = (int) (device.getYCoordinate()*1.5);
			json += "\t\"x\":" + deviceX + ",\n\t\"y\":" + deviceY + ",\n";
			if (device instanceof Router) {
				json += "\t\"group\": \"routerDevice\"\n";
			} else if (device instanceof Cloud) {
				json += "\t\"group\": \"cloudDevice\"\n";
			} else if (device instanceof Pc) {
				json += "\t\"group\": \"pcDevice\"\n";
			} else {
				json += "\t\"group\": \"switchDevice\"\n";
			}

			String portsJson = "[";
			for (int i = 0; i < device.getPortCount(); i++) {
				Port port = device.getPortAt(i);
				if (port instanceof HostPort) {
					System.err.println("Port " + port.getName() + 
							" is an instance of HostPort " + port.getType().toString());
					try {
						HostPort hostPort = (HostPort) port;
						hostPort.getIpAddress().getDottedQuadString();
						if (!portsJson.equals("[")) {
							portsJson += ",\n \t \t";
						}
						portsJson += "{ \"portName\": \"" + hostPort.getName() + "\", ";
						portsJson += "\"portIpAddress\": \"" 
								+ hostPort.getIpAddress().getDottedQuadString() + "\",";
						portsJson += "\"portSubnetMask\": \"" 
								+ hostPort.getSubnetMask().getDottedQuadString() + "\"}";
					} catch (Throwable t) {
						System.err.println(t.getMessage());
					}
				} else {
					System.err.println("Port " + port.getName() + 
							" is not an instance of HostPort " + port.getType().toString());
				}
			}
			if (!portsJson.equals("[")) {
				portsJson += "]";
				json += ", \"ports\": " + portsJson + "\n";
			}
			json += "},\n";
		}
		if(json.endsWith(",\n"))
			json = json.substring(0, json.length()-2);
		json += "]\n";
		System.err.println("Devices : \n" + json);
		return json;
	}

	protected String getEdgesJson(Set<Device> devices) throws Exception {
		String json = "[ \n";
		Map<UUID,List<Device>> linkMappings = new HashMap<UUID,List<Device>>();

		Map<UUID,List<Device>> linkMappings2= new HashMap<UUID, List<Device>>();
		Map<String,List<Device>> linkMappingsStrings = new HashMap<String, List<Device>>();
		for (Device device : devices) {
			for (int i = 0; i < device.getPortCount(); i++) {
				Port port = device.getPortAt(i);
				Link currentLink = port.getLink();
				if (currentLink != null) {
					List<Device> deviceList = null;
					if (linkMappingsStrings.keySet().contains(currentLink.getObjectUUID().getDecoratedHexString())) {
						System.err.println("Link " + currentLink.getObjectUUID().getDecoratedHexString() + " is already here." );
						deviceList = linkMappingsStrings.get(currentLink.getObjectUUID().getDecoratedHexString());	
					} else {
						deviceList = new ArrayList<Device>();
					}
					System.err.println("new edges: adding link " + currentLink.getObjectUUID().getDecoratedHexString() + " for device " + device.getObjectUUID().getDecoratedHexString());
					deviceList.add(device);
					linkMappings2.put(currentLink.getObjectUUID(), deviceList);
					linkMappingsStrings.put(currentLink.getObjectUUID().getDecoratedHexString(), deviceList);
				}
			}
		}
		//this works but is slow
		/*for (Device device : devices) {
			for (int i = 0; i < device.getPortCount(); i++) {
				Port port = device.getPortAt(i);
				Link currentLink = port.getLink();
				if (currentLink != null) {
					for (Device remoteDevice : devices) {
						boolean found = false;
						if (found) {
							break;
						} else {
							for (int j = 0; j < remoteDevice.getPortCount(); j++) {
								Link remoteLink = remoteDevice.getPortAt(j).getLink();
								if (remoteLink != null && !remoteDevice.equals(device)) {
									if (remoteLink.getObjectUUID().equals(currentLink.getObjectUUID())) {
										List<Device> pairing = new ArrayList<Device>();
										pairing.add(device);
										pairing.add(remoteDevice);
										linkMappings.put(currentLink.getObjectUUID(), pairing);
										found = true;
									}
								}
								if (found) {
									break;
								}
							}
						}
					}
				}
//				String remoteName = port.getRemotePortName();
//				UUID remoteUUID = null;
//				dumpPort(port);
//				for (Device remoteDevice : devices) {
//					boolean found = false;
//					if (!found) {
//						for (int j = 0; j < remoteDevice.getPortCount(); j++) {
//							String currentRemoteName = port.getName();
//							if (currentRemoteName.equals(remoteName)) {
//								remoteUUID = remoteDevice.getObjectUUID();
//								found = true;
//								break;
//							}
//							if (found) {
//								break;
//							}
//						}
//					}
//				}

			}
		}*/ //this works but is slow
		linkMappings = linkMappings2;

		List<String> done = new ArrayList<String>();
		for (UUID remoteUUID : linkMappings.keySet()) {
			if (remoteUUID != null && !done.contains(remoteUUID.getDecoratedHexString())) {
				List<Device> devicePair = linkMappings.get(remoteUUID);
				String linkUUIDString = remoteUUID.getDecoratedHexString();
				done.add(linkUUIDString);
				System.err.println("devicePair has " + devicePair.size() + " elements.");
				String fromUUIDString = devicePair.get(0).getObjectUUID().getDecoratedHexString();
				String toUUIDString = devicePair.get(1).getObjectUUID().getDecoratedHexString();
				json += " {\n \t\"id\": \"" + linkUUIDString 
						+ "\",\n\t\"from\": \"" + fromUUIDString + "\",\n "
						+ "\n\t\"to\": \"" + toUUIDString + "\"\n " 
						+ "},\n";
			}
		}
		if(json.endsWith(",\n"))
			json = json.substring(0, json.length()-2);
		json += "]\n";
		System.err.println("Edges: \n" + json);
		return json;
	}

	protected void dumpDeviceProcesses(Device device) throws Exception {
		System.out.println("\nProcesses");
		for (String s : PROCESS_NAMES) {
			dumpDeviceProcess(s, device.getProcess(s));
		}
	}

	protected void dumpPorts(Device device) throws Exception {
		System.out.println("\nPorts");
		int portCount = device.getPortCount();
		for (int i = 0; i < portCount; i++) {
			Port port = device.getPortAt(i);
			dumpPort(port);
			dumpPortProcess("KeepAlive", port.getKeepAliveProcess());
			dumpPortProcess("Encap", port.getEncapProcess());
		}
	}

	protected void dumpPort(Port port) throws Exception {
		String className = port.getClassName();
		String portName = port.getName();
		System.out.println("\n\tremotePort = " + port.getRemotePortName());
		//			System.out.println("\n\tname = " + portName);
		//			System.out.println("\ttype = " + port.getType());
		//			System.out.println("\tclass = " + port.getClass().getName());
		//			System.out.println("\tgetClassName() = " + className);
		dumpLink(port.getLink());

	}

	protected void dumpDeviceProcess(String name, Process process) throws Exception {
		if (process == null) {
			return;
		}
		System.out.println("\n\tDevice Process " + name);
		System.out.println("\tclass = " + process.getClass().getName());
		System.out.println("\tgetClassName() = " + process.getClassName());
	}

	protected void dumpPortProcess(String name, Process process) throws Exception {
		if (process == null) {
			return;
		}
		System.out.println("\n\t\tPort Process " + name);
		System.out.println("\t\tclass = " + process.getClass().getName());
		System.out.println("\t\tgetClassName() = " + process.getClassName());
	}

	protected void dumpLink(Link link) throws Exception {
		if (link == null) {
			return;
		}
		try {
			link.getClassName();
		} catch (Throwable t) {
			return;
		}

		System.out.println("\n\t\tLink");
		System.out.println("\t\tclass = " + link.getClass().getName());
		System.out.println("\t\tgetClassName() = " + link.getClassName());
		System.out.println("\t\tgetObjectUUID() = " + link.getObjectUUID());
	}

	protected boolean shouldLaunchPacketTracer() {
		return false;
	}

	@Override
	protected Log getLog() {
		return LOGGER;
	}

	private static final Log LOGGER = LogFactory.getLog(PtSmith.class);

	protected static final List<String> PROCESS_NAMES = createProcessNames();

	protected static List<String> createProcessNames() {
		List<String> answer = new ArrayList<String>();
		answer.add(IPCConstants.ACL_PROCESS);
		answer.add(IPCConstants.ARP_PROCESS);
		answer.add(IPCConstants.CDP_PROCESS);
		answer.add(IPCConstants.DHCP_CLIENT_PROCESS);
		answer.add(IPCConstants.DHCP_SERVER_PROCESS);
		answer.add(IPCConstants.EIGRP_MAIN_PROCESS);
		answer.add(IPCConstants.HOST_IP_PROCESS);
		answer.add(IPCConstants.HTTP_SERVER_PROCESS);
		answer.add(IPCConstants.ICMP_PROCESS);
		answer.add(IPCConstants.LOOPBACK_MANAGER_PROCESS);
		answer.add(IPCConstants.MAC_SWITCHER_PROCESS);
		answer.add(IPCConstants.NAT_PROCESS);
		answer.add(IPCConstants.OSPF_MAIN_PROCESS);
		answer.add(IPCConstants.RIP_PROCESS);
		answer.add(IPCConstants.ROUTING_PROCESS);
		answer.add(IPCConstants.TELNET_CLIENT_PROCESS);
		answer.add(IPCConstants.TELNET_SERVER_PROCESS);
		answer.add(IPCConstants.TFTP_SERVER_PROCESS);
		answer.add(IPCConstants.VLAN_MANAGER_PROCESS);
		answer.add(IPCConstants.VTP_PROCESS);
		answer.add(IPCConstants.WIRELESS_CLIENT_PROCESS);
		return answer;
	}
}

package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.ipc.enums.ConnectType;
import com.cisco.pt.ipc.sim.Network;
import com.cisco.pt.ipc.ui.LogicalWorkspace;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Link;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

abstract class LinkHandler extends PTCallable<Link> {
    final PortManager portMngr;
    public LinkHandler(String deviceId, String portName) {
        this.portMngr = new PortManager(deviceId, portName, this.task);
    }

    public Device getDevice() {
        return this.portMngr.getDevice();
    }
    public Port getPort() {
        return this.portMngr.getPort();
    }
    protected String getLinkId() {
        final Port port = getPort();
        if (port==null) {
            return null;
        }
        return port.getLink();
    }

    public Link getLink() {
        final Link ret = new Link();
        ret.setId(getLinkId());
        final Network network = this.task.getIPC().network();
        for (int i = 0; i < network.getDeviceCount(); i++) {
            final com.cisco.pt.ipc.sim.Device d = network.getDeviceAt(i);
            final String dId = d.getObjectUUID().getDecoratedHexString();
            if (!getDevice().getId().equals(dId))
                for(int j=0; j<d.getPortCount(); j++) {
                    final com.cisco.pt.ipc.sim.port.Port p = d.getPortAt(j);
                    final String lId = (p.getLink()==null)? null: p.getLink().getObjectUUID().getDecoratedHexString();
                    if (lId!=null && ret.getId().equals(lId)) {
                        ret.setToDevice(d.getName());
                        ret.setToPort(p.getName());
                        break;
                    }
                }
        }
        return ret;
    }
}

class LinkGetter extends LinkHandler {
    public LinkGetter(String deviceId, String portName) {
        super(deviceId, portName);
    }
    @Override
    public Link internalRun() {
        return getLink();
    }
}

class LinkDeleter extends LinkHandler {
    public LinkDeleter(String deviceId, String portName) {
        super(deviceId, portName);
    }
    @Override
    public Link internalRun() {
        if (getLinkId()==null) {
            // TODO throw appropriate exception!
            return null;
        }
        final LogicalWorkspace workspace = this.task.getIPC().appWindow().getActiveWorkspace().getLogicalWorkspace();
        final Device device = getDevice();
        final Port port = getPort();
        final boolean success = workspace.deleteLink(device.getLabel(), port.getPortName());
        if (success) {
            return getLink();
        }

        getLog().error("Unsuccessful deletion of link in " + device.getLabel() + ":" + port.getPortName() + ".");
        // TODO throw appropriate exception!
        return null;
    }
}

class LinkCreator extends LinkHandler {
    final Link linkToCreate;
    public LinkCreator(String deviceId, String portName, Link linkToCreate) {
        super(deviceId, portName);
        this.linkToCreate = linkToCreate;

    }
    @Override
    public Link internalRun() {
        if (getLinkId()!=null) {
            getLog().error("A link already exist for " + this.getDevice().getLabel() + ":" + getPort().getPortName() + ". Please, delete it first.");
            // TODO throw appropriate exception!
            return null;
        }
        final LogicalWorkspace workspace = this.task.getIPC().appWindow().getActiveWorkspace().getLogicalWorkspace();
        final Device device = this.portMngr.getDevice();
        final Port port = this.portMngr.getPort();
        // String deviceName1, String portName1, String deviceName2, String portName2, ConnectType connType
        getLog().error("Everything ok: " + this.linkToCreate.getToDevice() + ", " + this.linkToCreate.getToPort());
        final boolean success = workspace.createLink(device.getLabel(), port.getPortName(), this.linkToCreate.getToDevice(), this.linkToCreate.getToPort(), ConnectType.ETHERNET_STRAIGHT);
        if (success) {
            this.portMngr.reloadPort();
            return getLink();
        }

        getLog().error("Unsuccessful creation of link between " + device.getLabel() + ":" + port.getPortName() +
                " and " + this.linkToCreate.getToDevice() + ":" + this.linkToCreate.getToPort() + ".");
        // TODO throw appropriate exception!
        return null;
    }
}

@Path("devices/{device}/ports/{port}/link")
public class LinkResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Link getLink(@PathParam("device") String deviceId,
                          @PathParam("port") String portName) {
        return new LinkGetter(deviceId, portName).call();
    }
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Link removeLink(@PathParam("device") String deviceId,
                             @PathParam("port") String portName) {
        return new LinkDeleter(deviceId, portName).call();
    }
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Link createLink(Link newLink,
                             @PathParam("device") String deviceId,
                             @PathParam("port") String portName) {
        return new LinkCreator(deviceId, portName, newLink).call();
    }
}

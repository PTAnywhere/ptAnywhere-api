package uk.ac.open.kmi.forge.webPacketTracer;

import com.cisco.pt.ipc.enums.ConnectType;
import com.cisco.pt.ipc.ui.LogicalWorkspace;
import uk.ac.open.kmi.forge.webPacketTracer.gateway.PTCallable;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Device;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.LinkCreation;
import uk.ac.open.kmi.forge.webPacketTracer.pojo.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

abstract class LinkHandler extends PTCallable<String> {
    final PortGetter pGetter;
    public LinkHandler(String deviceId, String portName) {
        this.pGetter = new PortGetter(deviceId, portName);
    }
    public Device getDevice() {
        return this.pGetter.getDevice();
    }
    public Port getPort() {
        return this.pGetter.getPort();
    }
    public String getLinkId() {
        final Port port = getPort();
        if (port==null) {
            return null;
        }
        return port.getLink();
    }
}

class LinkGetter extends LinkHandler {
    public LinkGetter(String deviceId, String portName) {
        super(deviceId, portName);
    }
    @Override
    public String internalRun() {
        return getLinkId();
    }
}

class LinkDeleter extends LinkHandler {
    public LinkDeleter(String deviceId, String portName) {
        super(deviceId, portName);
    }
    @Override
    public String internalRun() {
        if (getLinkId()==null) {
            // TODO throw appropriate exception!
            return null;
        }
        final LogicalWorkspace workspace = this.task.getIPC().appWindow().getActiveWorkspace().getLogicalWorkspace();
        final Device device = this.pGetter.getDevice();
        final Port port = this.pGetter.getPort();
        final boolean success = workspace.deleteLink(device.getLabel(), port.getPortName());
        if (success) {
            return port.getLink();
        }

        getLog().error("Unsuccessful deletion of link in " + device.getLabel() + ":" + port.getPortName() + ".");
        // TODO throw appropriate exception!
        return null;
    }
}

class LinkCreator extends LinkHandler {
    final LinkCreation linkToCreate;
    public LinkCreator(String deviceId, String portName, LinkCreation linkToCreate) {
        super(deviceId, portName);
        this.linkToCreate = linkToCreate;

    }
    @Override
    public String internalRun() {
        if (getLinkId()!=null) {
            getLog().error("A link already exist for " + this.getDevice().getLabel() + ":" + getPort().getPortName() + ". Please, delete it first.");
            // TODO throw appropriate exception!
            return null;
        }
        final LogicalWorkspace workspace = this.task.getIPC().appWindow().getActiveWorkspace().getLogicalWorkspace();
        final Device device = this.pGetter.getDevice();
        final Port port = this.pGetter.getPort();
        // String deviceName1, String portName1, String deviceName2, String portName2, ConnectType connType
        getLog().error("Everything ok: " + this.linkToCreate.getToDevice() + ", " + this.linkToCreate.getToPort());
        final boolean success = workspace.createLink(device.getLabel(), port.getPortName(), this.linkToCreate.getToDevice(), this.linkToCreate.getToPort(), ConnectType.ETHERNET_STRAIGHT);
        if (success) {
            return "new-id"; // TODO return the correct id
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
    public String getLink(@PathParam("device") String deviceId,
                          @PathParam("port") String portName) {
        return new LinkGetter(deviceId, portName).call();
    }
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public String removeLink(@PathParam("device") String deviceId,
                            @PathParam("port") String portName) {
        return new LinkDeleter(deviceId, portName).call();
    }
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String createLink(LinkCreation newLink,
                             @PathParam("device") String deviceId,
                             @PathParam("port") String portName) {
        return new LinkCreator(deviceId, portName, newLink).call();
    }
}

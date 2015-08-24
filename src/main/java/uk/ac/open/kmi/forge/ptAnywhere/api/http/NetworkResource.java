package uk.ac.open.kmi.forge.ptAnywhere.api.http;

import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.ErrorBean;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.PacketTracerConnectionException;
import uk.ac.open.kmi.forge.ptAnywhere.exceptions.SessionNotFoundException;
import uk.ac.open.kmi.forge.ptAnywhere.gateway.PTCallable;
import uk.ac.open.kmi.forge.ptAnywhere.pojo.Network;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionManager;


class NetworkGetter extends PTCallable<Network> {

    final URLFactory uf;

    public NetworkGetter(SessionManager sm, URI baseURI) {
        super(sm);
        this.uf = new URLFactory(baseURI, sm.getSessionId());
    }

    @Override
    public Network internalRun() {
        return this.connection.getDataAccessObject().
                getWholeNetwork().setURLFactory(this.uf);
    }
}


@Api(hidden = true, tags = "network")
public class NetworkResource {

    final UriInfo uri;
    final SessionManager sm;
    public NetworkResource(UriInfo uri, SessionManager sm) {
        this.uri = uri;
        this.sm = sm;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieves information of the current network topology", response = Network.class)
    @ApiResponses(value = {
        @ApiResponse(code = PacketTracerConnectionException.status, response = ErrorBean.class, message = PacketTracerConnectionException.description),
        @ApiResponse(code = SessionNotFoundException.status, response = ErrorBean.class, message = SessionNotFoundException.description)
    })
    public Response getAll() {
        final Network network = new NetworkGetter(this.sm, this.uri.getBaseUri()).call();  // No Threads
        return addDefaultLinks(Response.ok(network)).build();
    }

    private Response.ResponseBuilder addDefaultLinks(Response.ResponseBuilder rb) {
        final URI sessionURL = Utils.getParent(this.uri.getRequestUri());
        return rb.link(sessionURL, "session").link(sessionURL + "devices", "devices");
    }
}

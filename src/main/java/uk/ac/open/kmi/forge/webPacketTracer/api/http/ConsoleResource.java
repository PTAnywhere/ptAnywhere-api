package uk.ac.open.kmi.forge.webPacketTracer.api.http;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.*;

@Path("devices/{device}/console")
public class ConsoleResource {

    @Context
    ServletContext ctx;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream getDevice(@PathParam("device") String deviceId) {
        return this.ctx.getResourceAsStream("widget/console.html");
    }
}
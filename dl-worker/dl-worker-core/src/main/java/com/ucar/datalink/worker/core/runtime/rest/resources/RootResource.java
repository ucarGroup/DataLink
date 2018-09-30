package com.ucar.datalink.worker.core.runtime.rest.resources;

import com.ucar.datalink.worker.core.runtime.rest.entities.ServerInfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class RootResource {

    @GET
    @Path("/")
    public ServerInfo serverInfo() {
        return new ServerInfo();
    }
}

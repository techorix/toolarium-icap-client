package de.techorix.icap;


import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/v1/virus-checker")
public class IcapVirusCheckResource {

    @Inject
    Logger log;

    @Inject
    IcapVirusCheckService virusCheck;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response checkVirus(@MultipartForm MultipartBody payload) {

        try {
            if (virusCheck.isInfected(payload.name, payload.file)) {
                return Response.status(Response.Status.CONFLICT).build();
            }
            log.debug("No virus in " + payload.name);
            return Response.ok("no virus detected").build();
        } catch (RuntimeException e) {
            log.debug("InternalError", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String returnVersion() {
        return "this is api v1 of virus checker";
    }
}

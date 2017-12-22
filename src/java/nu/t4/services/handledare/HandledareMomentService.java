package nu.t4.services.handledare;

import javax.ejb.EJB;
import javax.json.JsonArray;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nu.t4.beans.global.APLManager;
import nu.t4.beans.global.MomentManager;

/**
 *
 * @author maikwagner
 * @author Daniel Lundberg
 */
@Path("handledare")
public class HandledareMomentService {

    @EJB
    MomentManager momentManager;
    @EJB
    APLManager manager;

    @GET
    @Path("/moment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMomentPerHandledare(@Context HttpHeaders headers) {
        //Kollar att inloggningen är ok
        String basic_auth = headers.getHeaderString("Authorization");

        if (!manager.handledarAuth(basic_auth)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        int handledar_id = manager.getHandledarId(basic_auth);
        JsonArray moment = momentManager.getMomentPerHandledare(handledar_id);
        if (moment != null) {
            return Response.ok(moment).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
    }
}

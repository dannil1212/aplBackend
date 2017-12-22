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
import nu.t4.beans.global.AktivitetManager;

/**
 *
 * @author maikwagner
 */
@Path("handledare")
public class HandledareAktivitetService {

    @EJB
    APLManager manager;
    @EJB
    AktivitetManager aktivitetManager;

    @GET
    @Path("/aktiviteter")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAktiviteter(@Context HttpHeaders headers) {
        //Kollar att inloggningen är ok
        String basic_auth = headers.getHeaderString("Authorization");

        if (!manager.handledarAuth(basic_auth)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        //Hämtar handledaraktiviteter med 0
        int id = manager.getHandledarId(basic_auth);
        int HANDLEDARE = 0;

        JsonArray aktiviteter = aktivitetManager.getAktiviteter(id, HANDLEDARE);
        if (aktiviteter != null) {
            return Response.ok(aktiviteter).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}

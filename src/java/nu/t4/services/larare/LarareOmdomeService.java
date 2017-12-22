package nu.t4.services.larare;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import javax.ejb.EJB;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nu.t4.beans.global.APLManager;
import nu.t4.beans.larare.LarareOmdomeManager;

/**
 *
 * @author maikwagner
 */
@Path("larare")
public class LarareOmdomeService {

    @EJB
    LarareOmdomeManager larareOmdomeManager;

    @EJB
    APLManager manager;

    @GET
    @Path("/klass/{id}/omdome")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOmdome(@Context HttpHeaders headers, @PathParam("id") int id) {
        //Kollar att inloggningen är ok
        String idTokenString = headers.getHeaderString("Authorization");

        GoogleIdToken.Payload payload = manager.googleAuth(idTokenString);
        if (payload == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        JsonObject user = manager.getGoogleUser(payload.getSubject());
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        int behorighet = user.getInt("behorighet");

        if (behorighet != 1) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        JsonArray data = larareOmdomeManager.getOmdome(id);
        if (data != null) {
            return Response.ok(data).build();
        } else {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/klass/{id}/omdome/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOmdomeNew(@Context HttpHeaders headers,
            @PathParam("id") int klass_id, @PathParam("year") int year, @PathParam("month") int month) {
        //Kollar att inloggningen är ok
        String idTokenString = headers.getHeaderString("Authorization");

        GoogleIdToken.Payload payload = manager.googleAuth(idTokenString);
        if (payload == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        JsonObject user = manager.getGoogleUser(payload.getSubject());
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        int behorighet = user.getInt("behorighet");

        if (behorighet != 1) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        JsonArray data = larareOmdomeManager.getOmdome(klass_id, year, month);
        if (data != null) {
            return Response.ok(data).build();
        } else {
            return Response.serverError().build();
        }
    }
}

package nu.t4.services.larare;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import nu.t4.beans.global.APLManager;
import nu.t4.beans.global.NarvaroManager;
import javax.ejb.EJB;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("larare")
public class LarareNarvaroService {

    @EJB
    APLManager aplManager;
    @EJB
    NarvaroManager narvaroManager;

    @GET
    @Path("/klass/{id}/narvaro")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getGodkandNarvaroLarare(@Context HttpHeaders headers, @PathParam("id") int klass_id) {
        //Kollar att inloggningen är ok
        String idTokenString = headers.getHeaderString("Authorization");

        GoogleIdToken.Payload payload = aplManager.googleAuth(idTokenString);
        if (payload == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        JsonObject user = aplManager.getGoogleUser(payload.getSubject());
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        int behorighet = user.getInt("behorighet");

        if (behorighet != 1) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        int larare_id = user.getInt("id");

        JsonArray narvaro = narvaroManager.getGodkandNarvaro(larare_id, klass_id);
        if (narvaro != null) {
            return Response.ok(narvaro).build();
        } else {
            return Response.serverError().build();
        }
    }
}

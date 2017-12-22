package nu.t4.services.elev;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import java.io.StringReader;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
@Path("elev")
public class ElevMomentService {

    @EJB
    MomentManager momentManager;
    @EJB
    APLManager manager;

    @GET
    @Path("/moment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMomentElev(@Context HttpHeaders headers) {
        //Kollar att inloggningen är ok
        String idTokenString = headers.getHeaderString("Authorization");

        GoogleIdToken.Payload payload = manager.googleAuth(idTokenString);
        if (payload == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        JsonObject elev = manager.getGoogleUser(payload.getSubject());
        if (elev == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        int anvandar_id = elev.getInt("id");

        JsonArray moment = momentManager.getMomentElev(anvandar_id);
        if (moment != null) {
            return Response.ok(moment).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

    }

    @POST
    @Path("/moment/avklara")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response skickaMomentTillHandledare(@Context HttpHeaders headers, String body) {
        //Kollar att inloggningen är ok
        String idTokenString = headers.getHeaderString("Authorization");

        GoogleIdToken.Payload payload = manager.googleAuth(idTokenString);
        if (payload == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        JsonObject elev = manager.getGoogleUser(payload.getSubject());
        if (elev == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        //Skapa ett json objekt av indatan
        JsonReader jsonReader = Json.createReader(new StringReader(body));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();

        int moment_id = object.getInt("id");
        int anvandar_id = elev.getInt("id");

        if (momentManager.skickaMomentTillHandledare(moment_id, anvandar_id)) {
            return Response.ok().build();
        } else {
            return Response.serverError().build();
        }
    }
}

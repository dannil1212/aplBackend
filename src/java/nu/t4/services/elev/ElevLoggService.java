package nu.t4.services.elev;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import java.io.StringReader;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nu.t4.beans.global.APLManager;
import nu.t4.beans.elev.ElevLoggManager;

/**
 *
 * @author Daniel Nilsson
 */
@Path("elev")
public class ElevLoggService {

    @EJB
    APLManager manager;
    @EJB
    ElevLoggManager loggManager;

    @GET
    @Path("/logg")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLoggar(@Context HttpHeaders headers) {

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

        JsonArray data = loggManager.getLoggar(anvandar_id);
        if (data != null) {
            return Response.ok(data).build();
        } else {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/logg")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postLogg(@Context HttpHeaders headers, String body) {
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
        JsonObject logg = jsonReader.readObject();
        jsonReader.close();

        System.out.println();
        int id = elev.getInt("id");
        int ljus = logg.getInt("ljus");
        String datum = logg.getString("datum");
        String innehall = logg.getString("innehall");
        boolean privat;
        //Temporärt tills de loggböcker som skulle blivit skickade har blivit det.
        try {
            privat = logg.getBoolean("privat");
        } catch (Exception e) {
            privat = true;
        }
        JsonValue bildValue = logg.get("imgUrl");
        String bild = null;
        if (bildValue != JsonValue.NULL) {
            bild = bildValue.toString();
        }

        if (loggManager.postLogg(id, innehall, datum, ljus, bild, privat)) {
            return Response.status(Response.Status.CREATED).build();
        } else {
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("/logg/{id}/radera")
    public Response raderaNarvaro(@Context HttpHeaders headers, @PathParam("id") int loggbok_id) {

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
        int elev_id = user.getInt("id");

        if (loggManager.raderaLoggbok(loggbok_id, elev_id)) {
            return Response.status(Response.Status.ACCEPTED).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}

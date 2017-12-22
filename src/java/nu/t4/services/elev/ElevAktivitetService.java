package nu.t4.services.elev;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import java.io.StringReader;
import nu.t4.beans.larare.LarareHandledareManager;
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
import nu.t4.beans.global.AktivitetManager;

/**
 *
 * @author maikwagner
 * @author Daniel Lundberg
 */
@Path("elev")
public class ElevAktivitetService {

    @EJB
    APLManager manager;
    @EJB
    AktivitetManager aktivitetManager;

    @POST
    @Path("/aktivitet")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response uppdateraAktivitet(@Context HttpHeaders headers, String body) {
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
        JsonObject data = jsonReader.readObject();
        jsonReader.close();

        int typ = data.getInt("typ");
        int trafikljus;
        String innehall;
        switch (typ) {
            case 0:
                trafikljus = data.getInt("trafikljus");
                innehall = null;
                break;
            case 1:
                innehall = data.getString("innehall");
                trafikljus = -1;
                break;
            default:
                innehall = null;
                trafikljus = -1;
                break;
        }
        int elev_id = elev.getInt("id");
        int aktivitets_id = data.getInt("id");

        if (aktivitetManager.uppdateraElevAktivitet(typ, aktivitets_id, elev_id, trafikljus, innehall)) {
            return Response.ok().build();
        } else {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/aktiviteter")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNekadeAktiviteter(@Context HttpHeaders headers) {
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

        //Hämta nekade med 1
        int id = elev.getInt("id");
        int NEKADE = 1;

        JsonArray aktiviteter = aktivitetManager.getAktiviteter(id, NEKADE);
        if (aktiviteter != null) {
            return Response.ok(aktiviteter).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}

package nu.t4.services.larare;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import java.io.StringReader;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nu.t4.beans.global.APLManager;
import nu.t4.beans.larare.LarareRedigeraAnvManager;

/**
 *
 * @author Daniel Nilsson
 */
@Path("larare")
public class LarareRedigeraAnvService {

    @EJB
    APLManager manager;
    @EJB
    LarareRedigeraAnvManager anvManager;

    @POST
    @Path("/elev/redigera")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response redigeraElev(@Context HttpHeaders headers, String body) {
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
        //Skapa ett json objekt av indatan
        JsonReader jsonReader = Json.createReader(new StringReader(body));
        JsonObject obj = jsonReader.readObject();
        jsonReader.close();

        int id = obj.getInt("id");
        String namn = obj.getString("namn");
        String tfnr = obj.getString("tfnr");
        String email = obj.getString("email");
        int klass = obj.getInt("klass");
        int handledar_id = -1;
        if (!obj.isNull("hl_id")) {
            handledar_id = obj.getInt("hl_id");
        }
        if (anvManager.redigeraElev(id, namn, tfnr, email, klass, handledar_id)) {
            return Response.status(Response.Status.CREATED).build();
        } else {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/handledare/redigera")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response redigeraHandledare(@Context HttpHeaders headers, String body) {
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
        //Skapa ett json objekt av indatan
        JsonReader jsonReader = Json.createReader(new StringReader(body));
        JsonObject obj = jsonReader.readObject();
        jsonReader.close();

        int id = obj.getInt("id");
        String namn = obj.getString("namn");
        String tfnr = obj.getString("tfnr");
        String email = obj.getString("email");
        String foretag = obj.getString("foretag");
        String anvandarnamn = obj.getString("anvnamn");
        String losenord = obj.getString("losenord");

        if (anvManager.redigeraHandledare(id, namn, tfnr, email, foretag, anvandarnamn, losenord)) {
            return Response.status(Response.Status.CREATED).build();
        } else {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/koppla")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setElevHandledare(@Context HttpHeaders headers, String body) {
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

        //Skapa ett json objekt av indatan
        JsonReader jsonReader = Json.createReader(new StringReader(body));
        JsonArray array = jsonReader.readArray();
        jsonReader.close();

        if (anvManager.setElevHandledare(array)) {
            return Response.ok().build();
        } else {
            return Response.serverError().build();
        }
    }
}

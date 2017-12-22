package nu.t4.services.global;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import java.io.StringReader;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nu.t4.beans.global.APLManager;
import nu.t4.beans.global.KommentarManager;

/**
 *
 * @author Daniel Nilsson
 */
@Path("kommentar")
public class KommentarService {

    @EJB
    APLManager manager;
    @EJB
    KommentarManager kommentarManager;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postLogg(@Context HttpHeaders headers, String body) {
        //Kollar att inloggningen är ok
        String idTokenString = headers.getHeaderString("Authorization");
        
        GoogleIdToken.Payload payload = manager.googleAuth(idTokenString);
        if (payload == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        JsonObject anvandare = manager.getGoogleUser(payload.getSubject());
        if (anvandare == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        //Skapa ett json objekt av indatan
        JsonReader jsonReader = Json.createReader(new StringReader(body));
        JsonObject kommentarObjekt = jsonReader.readObject();
        jsonReader.close();

        //Hämtar användarens id mha den medskickade id_token
        int anvandar_id = anvandare.getInt("id");

        //Hämtar loggbok_id, innehållet och datum från objektet av indatan
        int loggbok_id = kommentarObjekt.getInt("loggbok_id");
        String innehall = kommentarObjekt.getString("innehall");
        String datum = kommentarObjekt.getString("datum");

        if (kommentarManager.postKommentar(anvandar_id, loggbok_id, innehall, datum)) {
            return Response.status(Response.Status.CREATED).build();
        } else {
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("{id}/radera")
    public Response raderaNarvaro(@Context HttpHeaders headers, @PathParam("id") int kommentar_id) {
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
        
        int anvandar_id = user.getInt("id");

        if (kommentarManager.raderaKommentar(kommentar_id, anvandar_id)) {
            return Response.status(Response.Status.ACCEPTED).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}

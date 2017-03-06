package nu.t4.services.larare;

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
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nu.t4.beans.global.APLManager;
import nu.t4.beans.larare.LarareKlassManager;

@Path("larare")
public class LarareKlassService {

    @EJB
    APLManager manager;
    @EJB
    LarareKlassManager larareManager;

    @GET
    @Path("/klasser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKlasser(@Context HttpHeaders headers) {
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

        int id = user.getInt("id");

        JsonArray klasser = larareManager.getKlasser(id);
        if (klasser != null) {
            return Response.ok(klasser).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/klass/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getElever(@Context HttpHeaders headers, @PathParam("id") int klass_id) {
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

        int anv_id = user.getInt("id");

        JsonArray aktiviteter = larareManager.getElever(anv_id, klass_id);
        if (aktiviteter != null) {
            return Response.ok(aktiviteter).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @POST
    @Path("/klasschange")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setKlass(@Context HttpHeaders headers, String body) {
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

        int anv_id = user.getInt("id");
        
         //Skapa ett json objekt av indatan
        JsonReader jsonReader = Json.createReader(new StringReader(body));
        JsonObject obj = jsonReader.readObject();
        jsonReader.close();

        int klass_id = obj.getInt("klass_id");

        if (larareManager.setKlass(anv_id, klass_id)) {
            return Response.status(Response.Status.CREATED).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}

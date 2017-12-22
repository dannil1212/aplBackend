package nu.t4.services.larare;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import java.io.StringReader;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
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
import nu.t4.beans.global.MomentManager;

/**
 *
 * @author maikwagner
 * @author Daniel Lundberg
 */
@Path("larare")
public class LarareMomentService {

    @EJB
    MomentManager momentManager;
    @EJB
    APLManager manager;

    @POST
    @Path("/moment")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response skapaMoment(@Context HttpHeaders headers, String body) {
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

        JsonReader jsonReader = Json.createReader(new StringReader(body));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();

        if (momentManager.skapaMoment(user.getInt("id"), object.getString("beskrivning"))) {
            return Response.status(201).build();
        } else {
            return Response.status(400).build();
        }

    }

    @GET
    @Path("/moment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response seMomentLarare(@Context HttpHeaders headers) {
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

        //Se till att användaren är en lärare
        int behorighet = user.getInt("behorighet");
        if (behorighet != 1) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        int larar_id = user.getInt("id");

        JsonArray moment = momentManager.seMomentLarare(larar_id);
        if (moment != null) {
            return Response.ok(moment).build();
        } else {
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("/moment/{id}")
    public Response raderaMoment(@Context HttpHeaders headers, @PathParam("id") int moment_id) {

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

        int larar_id = user.getInt("id");

        if (momentManager.raderaMomentLarare(moment_id, larar_id)) {
            return Response.status(Response.Status.ACCEPTED).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/moment/tilldela")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response tilldelaMoment(@Context HttpHeaders headers, String body) {
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

        JsonReader jsonReader = Json.createReader(new StringReader(body));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();

        JsonArray moment = object.getJsonArray("moment");
        JsonArray elever = object.getJsonArray("elever");

        if (momentManager.tilldelaMoment(moment, elever)) {
            return Response.status(201).build();
        } else {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/elev/{id}/moment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response visaElevsMoment(@Context HttpHeaders headers, @PathParam("id") int id) {
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
        JsonArray moment = momentManager.seMoment(id);
        if (moment != null) {
            return Response.ok(moment).build();
        } else {
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("/elev/{elev_id}/moment/{moment_id}")
    public Response raderaMomentElev(@Context HttpHeaders headers,
            @PathParam("moment_id") int moment_id, @PathParam("elev_id") int elev_id) {

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

        if (momentManager.raderaMomentElev(moment_id, elev_id)) {
            return Response.status(Response.Status.ACCEPTED).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}

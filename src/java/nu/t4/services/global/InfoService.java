package nu.t4.services.global;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
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
import nu.t4.beans.global.APLManager;
import nu.t4.beans.global.AnvInfoManager;
import nu.t4.beans.global.KontaktManager;

/**
 *
 * @author Daniel Nilsson
 */
@Path("info")
public class InfoService {

    @EJB
    APLManager manager;
    @EJB
    AnvInfoManager infoManager;
    @EJB
    KontaktManager kontaktManager;

    @GET
    @Path("/elev/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getElevInfo(@Context HttpHeaders headers, @PathParam("id") int id) {
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

        JsonObject data = infoManager.getElevInfo(id);
        if (data != null) {
            return Response.ok(data).build();
        } else {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/handledare/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHandledareInfo(@Context HttpHeaders headers, @PathParam("id") int id) {
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

        JsonObject data = infoManager.getHandledareInfo(id);
        if (data != null) {
            return Response.ok(data).build();
        } else {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/handledare/lista")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHandledare(@Context HttpHeaders headers) {
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

        int klass_id = user.getInt("klass");

        JsonArray data = infoManager.getHandledare(klass_id);
        if (data != null) {
            return Response.ok(data).build();
        } else {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/handledare/program/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHLPerProgram(@Context HttpHeaders headers, @PathParam("id") int id) {
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

        JsonArray data = infoManager.getHandledarePerProgram(id);
        if (data != null) {
            return Response.ok(data).build();
        } else {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/handledare/lista/alla")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHandledareAlla(@Context HttpHeaders headers) {
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

        int klass_id = user.getInt("klass");

        JsonArray data = infoManager.getHandledareAlla(klass_id);
        if (data != null) {
            return Response.ok(data).build();
        } else {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/elev/kontakt")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getElevKontakt(@Context HttpHeaders headers) {
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

        int anvandar_id = anvandare.getInt("id");

        JsonArray data = kontaktManager.getElevKontakt(anvandar_id);
        if (data != null) {
            return Response.ok(data).build();
        } else {
            return Response.serverError().build();
        }
    }

    @Path("/handledare/kontakt")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHLKontakt(@Context HttpHeaders headers) {
        //Kollar att inloggningen är ok
        String basic_auth = headers.getHeaderString("Authorization");

        if (!manager.handledarAuth(basic_auth)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        //Se till att användaren är en handledare
        int hl_id = manager.getHandledarId(basic_auth);
        if (hl_id == -1) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        JsonArray data = kontaktManager.getHLKontakt(hl_id);
        if (data != null) {
            return Response.ok(data).build();
        } else {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/larare/kontakt")
    @Produces(MediaType.APPLICATION_JSON)
    public Response kontaktLärare(@Context HttpHeaders headers) {
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

        int klass_id = user.getInt("klass");

        JsonArray data = kontaktManager.getKontaktLarare(klass_id);
        if (data != null) {
            return Response.ok(data).build();
        } else {
            return Response.serverError().build();
        }
    }
}

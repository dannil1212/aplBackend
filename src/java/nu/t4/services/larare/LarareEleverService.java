package nu.t4.services.larare;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import javax.ejb.EJB;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nu.t4.beans.APLManager;
import nu.t4.beans.GetLoggLärareManager;
import nu.t4.beans.LarareEleverManager;

@Path("larare")
public class LarareEleverService {

    @EJB
    APLManager manager;

    @EJB
    LarareEleverManager lararManager;

    @EJB
    GetLoggLärareManager loggLärareManager;

    @GET
    @Path("/elever")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getElever(@Context HttpHeaders headers) {
        String idTokenString = headers.getHeaderString("Authorization");
        GoogleIdToken.Payload payload = manager.googleAuth(idTokenString);
        if (payload == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        JsonObject user = manager.getGoogleUser(payload.getSubject());
        if (user == null) {

            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        int behörighet = user.getInt("behörighet");

        if (behörighet != 1) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        int klass_id = user.getInt("klass");

        JsonArray data = lararManager.getElever(klass_id);
        if (data != null) {
            return Response.ok(data).build();
        } else {
            return Response.serverError().build();
        }

    }

    @GET
    @Path("/elev/{id}/logg/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLog(@Context HttpHeaders headers, @PathParam("id") int elev_id) {
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

        int behörighet = user.getInt("behörighet");

        if (behörighet != 1) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        JsonArray data = loggLärareManager.getLoggar(user.getInt("id"), elev_id);
        if (data != null) {
            return Response.ok(data).build();
        } else {
            return Response.serverError().build();
        }

    }

}

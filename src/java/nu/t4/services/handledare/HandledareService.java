package nu.t4.services.handledare;

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
import nu.t4.beans.handledare.HandledareManager;

/**
 *
 * @author maikwagner
 */
@Path("handledare")
public class HandledareService {

    @EJB
    APLManager manager;
    @EJB
    AktivitetManager aktivitetManager;
    @EJB
    HandledareManager handledareManager;

    @POST
    @Path("/aktivitet")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response uppdateraAktivitet(@Context HttpHeaders headers, String body) {
        String basic_auth = headers.getHeaderString("Authorization");

        if (!manager.handledarAuth(basic_auth)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        JsonReader jsonReader = Json.createReader(new StringReader(body));
        JsonObject data = jsonReader.readObject();
        jsonReader.close();

        int aktivitets_id = data.getInt("id");
        int handledare_id = manager.getHandledarId(basic_auth);
        int godkant = data.getInt("godkant");
        int typ = data.getInt("typ");

        if (aktivitetManager.uppdateraAktivitet(typ, godkant, aktivitets_id, handledare_id)) {
            return Response.ok().build();
        } else {
            return Response.serverError().build();
        }
    }
    
    @GET
    @Path("/elever")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHLElever(@Context HttpHeaders headers) {
        String basic_auth = headers.getHeaderString("Authorization");

        if (!manager.handledarAuth(basic_auth)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        int handledare_id = manager.getHandledarId(basic_auth);
        JsonArray elever = handledareManager.getHLElever(handledare_id);
        if (elever != null) {
            return Response.ok(elever).build();
        } else {
            return Response.serverError().build();
        }
    }
}

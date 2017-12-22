package nu.t4.beans.global;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Base64;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import nu.t4.beans.ConnectionFactory;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author Daniel Nilsson
 */
@Stateless
public class APLManager {

    //id för vår app
    private final String CLIENT_id = "550162747744-m4r2h8egnvqicsbhoefdlo54lk8q399n.apps.googleusercontent.com";

    //Registrera ny elev
    public boolean registerGoogleUser(String googleid, String namn, int klass, String tfnr, String email) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format(
                    "INSERT INTO google_anvandare "
                    + "(google_id, namn, telefonnummer, email, klass, behorighet)  "
                    + "VALUES ('%s','%s','%s','%s',%d,0)",
                    googleid, namn, tfnr, email, klass
            );
            stmt.execute(sql);

            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //Verfiera googleinloggning & skicka tillbaka payload
    public GoogleIdToken.Payload googleAuth(String idTokenString) {
        //Varibler för verifiering
        HttpTransport httpTransport;
        JsonFactory jsonFactory;
        GoogleIdTokenVerifier verifier;
        try {
            //Skapa ny verifierare
            jsonFactory = JacksonFactory.getDefaultInstance();
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
                    .setAudience(Arrays.asList(CLIENT_id))
                    .build();
        } catch (Exception e) {
            return null;
        }
        GoogleIdToken idToken;
        try {
            //Verifiera
            idToken = verifier.verify(idTokenString);
        } catch (Exception ex) {
            return null;
        }

        //idToken blir null ifall den är felaktig
        if (idToken != null) {
            //Ta ut datan vi behöver från det verifierade idTokenet
            return idToken.getPayload();
            //if (payload.getHostedDomain().equals(APPS_DOMAIN_NAME)) {
            /*
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }*/
        } else {
            return null;
        }
    }

    //Verifiera handledareinloggning
    public boolean handledarAuth(String basic_auth) {
        try {
            //Ta bort "BASIC "
            basic_auth = basic_auth.substring(basic_auth.indexOf(" ") + 1, basic_auth.length());

            //Avkoda
            byte[] decoded = Base64.getDecoder().decode(basic_auth);
            String userPass = new String(decoded);

            //Ta ut anvnamn & lösenord
            String anvandarnamn = userPass.substring(0, userPass.indexOf(":"));
            String losenord = userPass.substring(userPass.indexOf(":") + 1, userPass.length());

            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            //Hämta handledaren med användarnamnet
            String sql = String.format(
                    "SELECT * FROM handledare WHERE anvandarnamn = '%s'",
                    anvandarnamn);
            ResultSet result = stmt.executeQuery(sql);
            result.next();
            //Verifiera lösenordet
            if (BCrypt.checkpw(losenord, result.getString("losenord"))) {
                conn.close();
                return true;
            } else {
                conn.close();
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //Hämta elev / lärare med google_id
    public JsonObject getGoogleUser(String google_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format(
                    "SELECT * FROM google_anvandare WHERE google_id = '%s'",
                    google_id);
            ResultSet result = stmt.executeQuery(sql);
            result.next();
            JsonObjectBuilder obuilder = Json.createObjectBuilder();
            obuilder.add("id", result.getInt("id"))
                    .add("namn", result.getString("namn"))
                    .add("tfnr", result.getString("telefonnummer"))
                    .add("email", result.getString("email"))
                    .add("klass", result.getInt("klass"))
                    .add("handledare_id", result.getInt("handledare_id"))
                    .add("senast_inloggad", result.getInt("senast_inloggad"))
                    .add("behorighet", result.getInt("behorighet"));
            conn.close();
            return obuilder.build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Ta bort användare med google_id eller användarnamn
    //      Endast för användning i automatiska test
    public boolean deleteUser(String key, boolean googleUser) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql;
            if (googleUser) {
                sql = String.format(
                        "DELETE FROM google_anvandare WHERE google_id = '%s'",
                        key);
            } else {
                sql = String.format(
                        "DELETE FROM handledare WHERE anvandarnamn = '%s'",
                        key);
            }
            stmt.executeUpdate(sql);
            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //Registrera ny handledare
    public boolean registerHandledare(String anvandarnamn, String namn, String losenord, String tfnr, String email, int program_id, String foretag) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            //Kryptera lösenord
            String encrypted_losenord = BCrypt.hashpw(losenord, BCrypt.gensalt());
            String sql = String.format(
                    "INSERT INTO handledare "
                    + "(namn, anvandarnamn, email, losenord, telefonnummer, program_id, foretag)"
                    + "VALUES ('%s','%s','%s','%s','%s', %d, '%s')",
                    namn, anvandarnamn, email, encrypted_losenord, tfnr, program_id, foretag
            );
            stmt.executeUpdate(sql);

            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //Hämtar alla klasser, används i registrering & lärare som byter klass
    public JsonArray getKlasser() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT * FROM klass ORDER BY namn");
            ResultSet data = stmt.executeQuery(sql);
            JsonArrayBuilder jBuilder = Json.createArrayBuilder();

            while (data.next()) {
                jBuilder.add(Json.createObjectBuilder()
                        .add("id", data.getInt("id"))
                        .add("namn", data.getString("namn"))
                        .build());
            }

            conn.close();
            return jBuilder.build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Hämtar handledare ID
    public int getHandledarId(String basic_auth) {
        try {
            //Ta bort "BASIC "
            basic_auth = basic_auth.substring(basic_auth.indexOf(" ") + 1, basic_auth.length());

            //Avkoda
            byte[] decoded = Base64.getDecoder().decode(basic_auth);
            String userPass = new String(decoded);

            //Hämta användarnamn
            String anvandarnamn = userPass.substring(0, userPass.indexOf(":"));

            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format(
                    "SELECT id FROM handledare WHERE anvandarnamn = '%s'",
                    anvandarnamn);
            ResultSet result = stmt.executeQuery(sql);
            result.next();
            int id = result.getInt("id");
            conn.close();
            return id;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    //Uppdatera senaste inloggning
    public void updateLastLogin(int id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("UPDATE google_anvandare SET senast_inloggad = now() "
                    + "WHERE id = %d", id);
            stmt.executeUpdate(sql);
            conn.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

package nu.t4.beans.global;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import nu.t4.beans.ConnectionFactory;

/**
 *
 * @author Daniel Nilsson
 */
@Stateless
public class KontaktManager {

    //Hämta kontakter för elev
    public JsonArray getElevKontakt(int elev_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT hlnamn AS namn, hlmail AS mail, "
                    + "hlnr AS tfnr FROM hlkontakt "
                    + "WHERE hlkontakt.id = (SELECT handledare_id FROM google_anvandare WHERE id = %d) "
                    + "UNION "
                    + "SELECT lararnamn AS namn, lararmail AS mail, lararnr AS tfnr FROM lararekontakt "
                    + "WHERE klass = "
                    + "(SELECT klass FROM google_anvandare WHERE id = %d)",
                    elev_id, elev_id);
            ResultSet data = stmt.executeQuery(sql);
            JsonArrayBuilder jBuilder = Json.createArrayBuilder();

            while (data.next()) {
                jBuilder.add(Json.createObjectBuilder()
                        .add("namn", data.getString("namn"))
                        .add("mail", data.getString("mail"))
                        .add("tfnr", data.getString("tfnr"))
                        .build()
                );
            }

            conn.close();
            return jBuilder.build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Hämta kontakter för lärare
    public JsonArray getKontaktLarare(int klass_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT "
                    + "google_anvandare.namn AS elevnamn, "
                    + "google_anvandare.email AS elevmail, "
                    + "google_anvandare.telefonnummer AS elevnummer, "
                    + "handledare.namn AS hlnamn, "
                    + "handledare.email AS hlmail, "
                    + "handledare.telefonnummer AS hlnummer "
                    + "FROM google_anvandare, handledare "
                    + "WHERE google_anvandare.handledare_id = handledare.id "
                    + "AND google_anvandare.klass = %d", klass_id);
            ResultSet data = stmt.executeQuery(sql);
            JsonArrayBuilder jsonArray = Json.createArrayBuilder();
            while (data.next()) {
                jsonArray.add(Json.createObjectBuilder()
                        .add("elevanamn", data.getString("elevnamn"))
                        .add("elevmail", data.getString("elevmail"))
                        .add("elevnummer", data.getString("elevnummer"))
                        .add("hlnamn", data.getString("hlnamn"))
                        .add("hlmail", data.getString("hlmail"))
                        .add("hlnummer", data.getString("hlnummer"))
                        .build());
            }
            conn.close();
            return jsonArray.build();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    //Hämta kontakter för handledare
    public JsonArray getHLKontakt(int hl_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT namn, email AS mail, "
                    + "telefonnummer AS tfnr, 0 AS behorighet FROM elevkontakt "
                    + "WHERE elevkontakt.id IN (SELECT id FROM google_anvandare WHERE handledare_id = %d) "
                    + "UNION "
                    + "SELECT lararnamn AS namn, lararmail AS mail, "
                    + "lararnr AS tfnr, 1 AS behorighet FROM lararekontakt "
                    + "WHERE klass IN (SELECT klass FROM google_anvandare WHERE handledare_id = %d)",
                    hl_id, hl_id);
            ResultSet data = stmt.executeQuery(sql);
            JsonArrayBuilder jBuilder = Json.createArrayBuilder();

            while (data.next()) {
                jBuilder.add(Json.createObjectBuilder()
                        .add("namn", data.getString("namn"))
                        .add("mail", data.getString("mail"))
                        .add("tfnr", data.getString("tfnr"))
                        .add("behorighet", data.getInt("behorighet"))
                        .build()
                );
            }

            conn.close();
            return jBuilder.build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}

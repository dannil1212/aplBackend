package nu.t4.beans.global;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import java.sql.ResultSet;
import java.util.Iterator;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;
import nu.t4.beans.ConnectionFactory;

/**
 *
 * @author maikwagner
 */
@Stateless
public class NarvaroManager {

    //Spara ny närvaro
    public boolean setNarvaro(JsonObject obj, int id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = (Statement) conn.createStatement();
            String sqlbase = "INSERT INTO narvaro "
                    + "(anvandar_id, trafikljus, godkand, datum) "
                    + "VALUES (%d, %d, 0, '%s')";
            String sql = "";

            int trafikljus = obj.getInt("trafikljus");
            String datum = obj.getString("datum");
            sql = String.format(sqlbase, id, trafikljus, datum);
            stmt.executeUpdate(sql);
            conn.close();
            return true;

        } catch (Exception e) {
            System.out.println("elevhandledare - setElevHandledare()");
            System.out.println(e.getMessage());
            return false;
        }
    }

    //Hämta ej nekad närvaro för alla elever i klass
    public JsonArray getGodkandNarvaro(int larare_id, int klass_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = (Statement) conn.createStatement();
            //Hämta elever i klassen om läraren är i samma program som klassen
            String sql = String.format("SELECT namn, id FROM google_anvandare "
                    + "WHERE behorighet = 0 AND klass = %d "
                    + "AND %d IN (SELECT id FROM klass "
                    + "WHERE program_id = (SELECT program_id FROM klass "
                    + "WHERE id = (SELECT klass FROM google_anvandare "
                    + "WHERE id = %d)))", klass_id, klass_id, larare_id);
            ResultSet data = stmt.executeQuery(sql);
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            while (data.next()) {
                int elev_id = data.getInt("id");
                String namn = data.getString("namn");
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("elev_id", elev_id)
                        .add("namn", namn)
                        .build());
            }
            Iterator<JsonValue> iterator = arrayBuilder.build().iterator();
            while (iterator.hasNext()) {
                JsonObject obj = (JsonObject) iterator.next();
                JsonArrayBuilder arrayBuilder2 = Json.createArrayBuilder();
                int elev_id = obj.getInt("elev_id");
                String namn = obj.getString("namn");

                //Hämta närvaron som ej är nekad
                sql = String.format("SELECT UNIX_TIMESTAMP(datum) AS datum, trafikljus, godkand FROM narvaro "
                        + "WHERE anvandar_id = %d AND godkand != 2 ORDER BY datum", elev_id);

                ResultSet data2 = stmt.executeQuery(sql);
                while (data2.next()) {
                    arrayBuilder2.add(Json.createObjectBuilder()
                            .add("datum", data2.getInt("datum"))
                            .add("trafikljus", data2.getInt("trafikljus"))
                            .add("godkant", data2.getInt("godkand"))
                            .build());
                }
                //Lägg ihop närvaron med elevens namn/id
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("elev_id", elev_id)
                        .add("namn", namn)
                        .add("narvaro", arrayBuilder2.build())
                        .build());
            }
            conn.close();
            return arrayBuilder.build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Hämta ej nekad närvaro för specifik elev
    public JsonArray getGodkandNarvaroElev(int elev_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = (Statement) conn.createStatement();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            String sql = String.format("SELECT UNIX_TIMESTAMP(datum) AS datum, trafikljus, godkand FROM narvaro "
                    + "WHERE anvandar_id = %d AND godkand != 2 ORDER BY datum", elev_id);

            ResultSet data = stmt.executeQuery(sql);
            while (data.next()) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("datum", data.getInt("datum"))
                        .add("trafikljus", data.getInt("trafikljus"))
                        .add("godkant", data.getInt("godkand"))
                        .build());
            }
            conn.close();
            return arrayBuilder.build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Ta bort närvaro
    public boolean raderaNarvaro(int narvaro_id, int elev_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            java.sql.Statement stmt = conn.createStatement();
            String sql = String.format("DELETE FROM narvaro WHERE narvaro_id = %d AND anvandar_id = %d", narvaro_id, elev_id);
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

};

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

    public JsonArray getNarvaro() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = (Statement) conn.createStatement();
            String sql = "SELECT * FROM narvaro";
            ResultSet data = stmt.executeQuery(sql);

            JsonArrayBuilder elever = Json.createArrayBuilder();

            while (data.next()) {
                elever.add(Json.createObjectBuilder()
                        .add("narvaro_id", data.getInt("narvaro_id"))
                        .add("anvandar_id", data.getInt("anvandar_id"))
                        .add("trafikljus", data.getInt("trafikljus"))
                        .add("godkand", data.getInt("godkand"))
                        .add("datum", data.getString("datum"))
                        .build());
            }

            conn.close();
            return elever.build();

        } catch (Exception e) {
            System.out.println("NarvaroManagar - getNarvaro()");
            System.out.println(e.getMessage());
            return null;
        }

    }

    public boolean setNarvaro(JsonObject array, int id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = (Statement) conn.createStatement();
            String sqlbase = "INSERT INTO narvaro "
                    + "(anvandar_id, trafikljus, godkand, datum) "
                    + "VALUES (%d, %d, 0, '%s')";
            String sql = "";

            JsonObject item = array;
            int trafikljus = item.getInt("trafikljus");
            String datum = item.getString("datum");
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

    public JsonArray getGodkandNarvaro(int larare_id, int klass_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = (Statement) conn.createStatement();
            String sql = String.format("SELECT namn, id FROM google_anvandare "
                    + "WHERE behorighet = 0 AND klass = %d AND %d IN (SELECT id FROM klass "
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

    public boolean raderaNarvaro(int narvaro_id, int elevid) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            java.sql.Statement stmt = conn.createStatement();
            String sql = String.format("DELETE FROM narvaro WHERE narvaro_id = %d AND anvandar_id = %d", narvaro_id, elevid);
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

};

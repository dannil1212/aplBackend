package nu.t4.beans.global;

import com.mysql.jdbc.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import nu.t4.beans.ConnectionFactory;

/**
 *
 * @author Daniel Nilsson
 */
@Stateless
public class KommentarManager {

    //Spara ny kommentar
    public boolean postKommentar(int anvandar_id, int loggbok_id,
            String innehall, String datum) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("INSERT INTO kommentar "
                    + "(anvandar_id, loggbok_id, innehall, datum) "
                    + "VALUES (%d,%d,'%s','%s')",
                    anvandar_id, loggbok_id, innehall, datum);
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //Hämta kommentarer för loggbok
    public JsonArray getKommentar(int logg_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM kommentarvy WHERE loggbok_id =" + logg_id;
            ResultSet data = stmt.executeQuery(sql);

            JsonArrayBuilder jsonArray = Json.createArrayBuilder();
            while (data.next()) {

                JsonObjectBuilder obuilder = Json.createObjectBuilder();
                obuilder.add("innehall", data.getString("innehall"))
                        .add("datum", data.getString("datum"))
                        .add("namn", data.getString("namn"));
                jsonArray.add(obuilder.build());
            }
            conn.close();
            return jsonArray.build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Ta bort kommentar
    public boolean raderaKommentar(int kommentar_id, int anvandar_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            java.sql.Statement stmt = conn.createStatement();
            String sql = String.format("DELETE FROM kommentar "
                    + "WHERE kommentar_id = %d "
                    + "AND anvandar_id = %d",
                    kommentar_id, anvandar_id);
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.t4.beans.elev;

import com.mysql.jdbc.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import nu.t4.beans.ConnectionFactory;

/**
 *
 * @author Daniel Nilsson
 */
@Stateless
public class ElevLoggManager {

    public JsonArray getLoggar(int elev_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            com.mysql.jdbc.Statement stmt = (com.mysql.jdbc.Statement) conn.createStatement();
            String sql = String.format("SELECT * FROM loggbokvy WHERE "
                    + "loggbokvy.elev_id = %d ORDER BY loggbokvy.datum DESC", elev_id);
            ResultSet data = stmt.executeQuery(sql);
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            while (data.next()) {
                String stringIntryck = "";
                int intryck = data.getInt("intryck");
                if (intryck == 0) {
                    stringIntryck = "dålig";
                } else if (intryck == 1) {
                    stringIntryck = "sådär";
                } else if (intryck == 2) {
                    stringIntryck = "bra";
                } else {
                    stringIntryck = "okänt";
                }
                JsonObjectBuilder obuilder = Json.createObjectBuilder();
                obuilder.add("id", data.getInt("id"))
                        .add("elev_id", data.getInt("elev_id"))
                        .add("innehall", data.getString("innehall"))
                        .add("intryck", stringIntryck)
                        .add("datum", data.getString("datum"))
                        .add("namn", data.getString("namn"))
                        .add("godkand", data.getString("godkand"));
                //Hanterar om "bild" är null i databasen
                String bild = data.getString("bild");
                if (data.wasNull()) {
                    obuilder.add("bild", JsonObject.NULL);
                } else {
                    obuilder.add("bild", bild);
                }
                arrayBuilder.add(obuilder.build());
            }
            JsonArray array1 = arrayBuilder.build();
            Iterator<JsonValue> iterator = array1.iterator();
            while (iterator.hasNext()) {
                JsonObject obj = (JsonObject) iterator.next();
                JsonArrayBuilder arrayBuilder2 = Json.createArrayBuilder();
                int logg_id = obj.getInt("id");
                sql = "SELECT * FROM kommentarvy WHERE loggbok_id =" + logg_id;
                ResultSet data2 = stmt.executeQuery(sql);
                JsonArrayBuilder jsonArray = Json.createArrayBuilder();
                while (data2.next()) {
                    String datum = data2.getString("datum").substring(0, 16);
                    JsonObjectBuilder obuilder = Json.createObjectBuilder();
                    obuilder.add("innehall", data2.getString("innehall"))
                            .add("datum", datum)
                            .add("namn", data2.getString("namn"));
                    arrayBuilder2.add(obuilder.build());
                }
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("id", logg_id)
                        .add("elev_id", obj.getInt("elev_id"))
                        .add("innehall", obj.getString("innehall"))
                        .add("intryck", obj.getString("intryck"))
                        .add("datum", obj.getString("datum"))
                        .add("namn", obj.getString("namn"))
                        .add("bild", obj.get("bild"))
                        .add("godkand", obj.get("godkand"))
                        .add("kommentarer", arrayBuilder2.build())
                        .build());
            }
            conn.close();
            return arrayBuilder.build();
        } catch (Exception e) {
            System.out.println("getLoggar");
            System.out.println(e.getMessage());
            return null;
        }
    }

    public boolean postLogg(int id, String innehall, String datum, int ljus, String bild, boolean privat) {
        try {
            java.sql.Connection conn = ConnectionFactory.getConnection();
            Statement stmt = (Statement) conn.createStatement();
            String sql = "";
            int status = privat ? 3 : 0; // 0 = ej godkänd, 3 = privat
            if (bild != null) {
                sql = String.format("INSERT INTO loggbok "
                        + "(elev_id, innehall, intryck, datum, bild, godkand) "
                        + "VALUES (%d,'%s',%d,'%s','%s', %d)",
                        id, innehall, ljus, datum, bild, status);
            } else {
                sql = String.format("INSERT INTO loggbok "
                        + "(elev_id, innehall, intryck, datum, godkand) "
                        + "VALUES (%d,'%s',%d,'%s', %d)",
                        id, innehall, ljus, datum, status);
            }
            stmt.executeUpdate(sql);
            conn.close();
            return true;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean raderaLoggbok(int loggbok_id, int elev_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            java.sql.Statement stmt = conn.createStatement();
            String sql = String.format("DELETE FROM loggbok WHERE id = %d AND elev_id = %d", loggbok_id, elev_id);
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}

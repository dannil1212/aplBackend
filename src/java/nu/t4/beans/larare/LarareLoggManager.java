/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.t4.beans.larare;

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
 * @author luan96001
 */
@Stateless
public class LarareLoggManager {

    public JsonArray getLoggar(int elev_id) {

        try {

            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT loggbok.*, google_anvandare.namn"
                    + " FROM loggbok, google_anvandare WHERE loggbok.elev_id = "
                    + "google_anvandare.id AND loggbok.elev_id = %d ORDER BY loggbok.datum DESC", elev_id);
            ResultSet data = stmt.executeQuery(sql);

            JsonArrayBuilder jsonArray = Json.createArrayBuilder();
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
                    stringIntryck = "FEL";
                }

                jsonArray.add(Json.createObjectBuilder()
                        .add("id", data.getInt("id"))
                        .add("elev_id", data.getInt("elev_id"))
                        .add("innehall", data.getString("innehall"))
                        .add("intryck", data.getInt("intryck"))
                        .add("datum", data.getInt("datum"))
                        .add("bild_id", data.getInt("bild_id"))
                        .build());
            }

            conn.close();
            return jsonArray.build();

        } catch (Exception e) {
            System.out.println(e.getMessage());

            return null;
        }
    }

    public JsonArray getLoggar(int techer_id, int elev_id) {

        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
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
            System.out.println("--Error from: GetLoggLarareManager: getLoggarFromTeacher--");
            System.out.println(e.getMessage());
            System.out.println("--End ErrorMessage--");
            return null;
        }
    }

}

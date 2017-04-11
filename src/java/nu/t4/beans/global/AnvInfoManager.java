/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.t4.beans.global;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import nu.t4.beans.ConnectionFactory;

/**
 *
 * @author Daniel Nilsson
 */
@Stateless
public class AnvInfoManager {

    public JsonObject getElevInfo(int elev_id) {

        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM elevinfo WHERE id = " + elev_id;
            ResultSet data = stmt.executeQuery(sql);
            data.next();
            JsonObjectBuilder obj = Json.createObjectBuilder();
            obj.add("namn", data.getString("namn"))
                    .add("tfnr", data.getString("tfnr"))
                    .add("email", data.getString("email"))
                    .add("klass", data.getInt("klass"));
            int hl_id = data.getInt("hl_id");
            if (data.wasNull()) {
                obj.add("hl_id", JsonObject.NULL);
            } else {
                obj.add("hl_id", hl_id);
            }

            conn.close();
            return obj.build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public JsonObject getHandledareInfo(int hl_id) {

        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM hlinfo WHERE id = " + hl_id;
            ResultSet data = stmt.executeQuery(sql);
            data.next();
            JsonObjectBuilder obj = Json.createObjectBuilder();
            obj.add("namn", data.getString("namn"))
                    .add("tfnr", data.getString("tfnr"))
                    .add("email", data.getString("email"))
                    .add("program_id", data.getInt("program_id"))
                    .add("foretag", data.getString("foretag"))
                    .add("anvnamn", data.getString("anvandarnamn"));

            conn.close();
            return obj.build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Hämtar alla lediga handledare
    public JsonArray getHandledare(int klass_id) { //Använder klassid för att hämta program

        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT id, namn, foretag FROM handledare "
                    + "where handledare.id NOT IN "
                    + "(SELECT handledare_id FROM google_anvandare "
                    + "WHERE handledare.id = aplapp.google_anvandare.handledare_id) "
                    + "AND handledare.program_id = (SELECT klass.program_id FROM klass WHERE klass.id = %d )"
                    + "ORDER BY namn", klass_id);
            ResultSet data = stmt.executeQuery(sql);
            JsonArrayBuilder jBuilder = Json.createArrayBuilder();
            while (data.next()) {
                String namn_foretag = data.getString("namn") + " - " + data.getString("foretag");
                jBuilder.add(Json.createObjectBuilder()
                        .add("id", data.getInt("id"))
                        .add("namn_foretag", namn_foretag)
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

    //Hämtar alla handledare i ditt program
    public JsonArray getHandledareAlla(int klass_id) { //Använder klassid för att hämta program

        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT id, namn, foretag FROM handledare "
                    + "WHERE handledare.program_id = (SELECT klass.program_id FROM klass WHERE klass.id = %d ) "
                    + "ORDER BY namn", klass_id);
            ResultSet data = stmt.executeQuery(sql);
            JsonArrayBuilder jBuilder = Json.createArrayBuilder();
            while (data.next()) {
                String namn_foretag = data.getString("namn") + " - " + data.getString("foretag");
                jBuilder.add(Json.createObjectBuilder()
                        .add("id", data.getInt("id"))
                        .add("namn_foretag", namn_foretag)
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

    public JsonArray getHandledarePerProgram(int id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT id, namn, foretag FROM handledare "
                    + "WHERE handledare.program_id = %d ", id);
            ResultSet data = stmt.executeQuery(sql);
            JsonArrayBuilder jBuilder = Json.createArrayBuilder();
            while (data.next()) {
                String namn_foretag = data.getString("namn") + " - " + data.getString("foretag");
                jBuilder.add(Json.createObjectBuilder()
                        .add("id", data.getInt("id"))
                        .add("namn_foretag", namn_foretag)
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

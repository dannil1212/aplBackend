/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.t4.beans.global;

import com.mysql.jdbc.Connection;
import java.sql.Statement;
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
 * @authorDaniel Lundberg
 */
@Stateless
public class MomentManager {

    /**
     * <h2>Skapa Moment</h2>
     * <p>
     * Funktion för att skapa moment, skall användas av lärare</p>
     *
     * @param user_id Lärarens id
     * @param Innehåll beskrivning av momentet
     * @return true om det skapats någonting annars false
     */
    public boolean skapaMoment(int user_id, String innehall) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            String sql = String.format("INSERT INTO aplapp.moment "
                    + "(anvandar_id, innehall) "
                    + "VALUES (%d,'%s')",
                    user_id, innehall);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println("Error from MomentManager:skapaMoment: " + e.getMessage());
            return false;
        }
    }

    /**
     * <h2>Se moment</h2>
     * <p>
     * Funktion för att se de moment som en elev har sig tilldelade</p>
     *
     * @param elev_id elevens id
     * @return jsonarray med [{moment:number, godkand:number},..]
     */
    public JsonArray seMoment(int elev_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            String sql = String.format("SELECT * FROM aplapp.koppla_moment_elev, moment WHERE koppla_moment_elev.anvandar_id = %d AND moment_id = moment.id;;", elev_id);
            Statement stmt = conn.createStatement();
            ResultSet data = stmt.executeQuery(sql);
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            while (data.next()) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("moment", data.getInt("moment_id"))
                        .add("innehall", data.getString("innehall"))
                        .add("godkand", data.getInt("godkand"))
                        .build()
                );
            }
            conn.close();
            return arrayBuilder.build();
        } catch (Exception e) {
            System.out.println("Error from MomentManager:seMoment: " + e.getMessage());
            return null;
        }

    }

    /**
     * <h2>Hämta lärarens moment</h2>
     * <p>
     * Funktion som hämtar alla momenten som den inloggade läraren har
     * skapat</p>
     *
     * @param lärar_id
     * @return jsonarray med [{id:number, innehall:varchar},..]
     */
    public JsonArray seMomentLarare(int larar_id) {

        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT id, innehall from moment WHERE anvandar_id =" + larar_id;
            ResultSet data = stmt.executeQuery(sql);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            while (data.next()) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("id", data.getInt("id"))
                        .add("innehall", data.getString("innehall"))
                        .build()
                );
            }
            conn.close();
            return arrayBuilder.build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * <h2>Radera lärarens moment</h2>
     * <p>
     * Funktion som raderar det valda momentet som läraren har skapat</p>
     *
     * @param moment_id
     * @param lärar_id
     * @return true om raderingen lyckas annars false
     */
    public boolean raderaMomentLarare(int moment_id, int larar_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("DELETE FROM moment WHERE id = %d AND anvandar_id = %d", moment_id, larar_id);
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * <h2>Radera elevens moment</h2>
     * <p>
     * Funktion som raderar det valda momentet som är tilldelat till eleven</p>
     *
     * @param moment_id
     * @param användar_id
     * @return true om raderingen lyckas annars false
     */
    public boolean raderaMomentElev(int moment_id, int anvandar_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("DELETE FROM koppla_moment_elev WHERE "
                    + "moment_id = %d AND anvandar_id = %d", moment_id, anvandar_id);
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * <h2>Tilldela Moment</h2>
     * <p>
     * funktion som kopplar moment med elever</p>
     *
     * @param moment json array av momenten
     * @param elever json array av eleverna
     * @return true om det skapats någonting annars false
     */
    public boolean tilldelaMoment(JsonArray moment, JsonArray elever) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();

            String sqlbase = "INSERT INTO koppla_moment_elev "
                    + "(anvandar_id, moment_id, godkand) "
                    + "VALUES (%d,%d,0);";
            Iterator<JsonValue> mIterator = moment.iterator();
            while (mIterator.hasNext()) {
                JsonObject momentet = (JsonObject) mIterator.next();
                Iterator<JsonValue> eIterator = elever.iterator();
                while (eIterator.hasNext()) {
                    JsonObject eleven = (JsonObject) eIterator.next();
                    int moment_id = momentet.getInt("moment_id");
                    int elev_id = eleven.getInt("elev_id");
                    String sql = String.format(sqlbase, elev_id, moment_id);
                    stmt.addBatch(sql);
                }
            }
            stmt.executeBatch();
            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println("Error from MomentManager:kopplaElev_Moment: " + e.getMessage());
            return false;
        }
    }

    public JsonArray getMomentElev(int id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            com.mysql.jdbc.Statement stmt = (com.mysql.jdbc.Statement) conn.createStatement();
            String sql = String.format(
                    "SELECT moment.innehall, koppla_moment_elev.godkand, koppla_moment_elev.moment_id "
                    + "FROM moment, koppla_moment_elev "
                    + "WHERE moment.id = koppla_moment_elev.moment_id AND koppla_moment_elev.anvandar_id =%d", id
            );

            ResultSet data = stmt.executeQuery(sql);

            JsonArrayBuilder elever = Json.createArrayBuilder();

            while (data.next()) {
                elever.add(Json.createObjectBuilder()
                        .add("innehall", data.getString("innehall"))
                        .add("godkand", data.getInt("godkand"))
                        .add("moment_id", data.getInt("moment_id"))
                        .build());
            }

            conn.close();
            return elever.build();

        } catch (Exception e) {
            System.out.println("MomentManager - getMomentElev()");
            System.out.println(e.getMessage());
            return null;
        }

    }

    public JsonArray getMomentPerHandledare(int handledar_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            com.mysql.jdbc.Statement stmt = (com.mysql.jdbc.Statement) conn.createStatement();
            String sql = String.format("SELECT moment.innehall, koppla_moment_elev.anvandar_id, "
                    + "koppla_moment_elev.godkand, koppla_moment_elev.moment_id "
                    + "FROM moment, koppla_moment_elev "
                    + "WHERE moment.id = koppla_moment_elev.moment_id "
                    + "AND koppla_moment_elev.anvandar_id IN "
                    + "(select id from google_anvandare where handledare_id = %d)",
                    handledar_id);
            ResultSet data = stmt.executeQuery(sql);

            JsonArrayBuilder moment = Json.createArrayBuilder();

            while (data.next()) {
                moment.add(Json.createObjectBuilder()
                        .add("id", data.getInt("moment_id"))
                        .add("elev_id", data.getInt("anvandar_id"))
                        .add("innehall", data.getString("innehall"))
                        .add("status", data.getInt("godkand"))
                        .build());
            }

            conn.close();
            return moment.build();

        } catch (Exception e) {
            System.out.println("elevhandledare - getElev()");
            System.out.println(e.getMessage());
            return null;
        }

    }

    public boolean skickaMomentTillHandledare(int moment_id, int anvandar_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            com.mysql.jdbc.Statement stmt = (com.mysql.jdbc.Statement) conn.createStatement();
            String sql = String.format("UPDATE koppla_moment_elev SET godkand = 1 "
                    + "WHERE moment_id = %d AND anvandar_id = %d", moment_id, anvandar_id);
            stmt.executeUpdate(sql);
            conn.close();
            return true;

        } catch (Exception e) {
            System.out.println("MomentManager - skickaMomentTillHandledare()");
            System.out.println(e.getMessage());
            return false;
        }
    }
}

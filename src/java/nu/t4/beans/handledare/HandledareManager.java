/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.t4.beans.handledare;

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
public class HandledareManager {

    public JsonArray getHLElever(int anv_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sqlBase = "SELECT namn, id FROM google_anvandare WHERE handledare_id = %d";
            String sql = String.format(sqlBase, anv_id);
            ResultSet data = stmt.executeQuery(sql);
            
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            int lastID = -1;
            while (data.next()) {
                JsonObjectBuilder obuilder = Json.createObjectBuilder();
                obuilder.add("namn", data.getString("namn"));
                obuilder.add("id", data.getInt("id"));
                arrayBuilder.add(obuilder.build());
            }
            conn.close();
            return arrayBuilder.build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}

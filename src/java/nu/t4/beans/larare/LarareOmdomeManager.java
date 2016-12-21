/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.t4.beans.larare;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import java.sql.ResultSet;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import nu.t4.beans.ConnectionFactory;

/**
 *
 * @author maikwagner
 */
@Stateless
public class LarareOmdomeManager {

    public JsonArray getOmdome(int klass_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = (Statement) conn.createStatement();
            String sql = String.format(
                    "SELECT * FROM intryckvy_month WHERE klass = %d", klass_id
            );
            ResultSet data = stmt.executeQuery(sql);
            JsonArrayBuilder array = Json.createArrayBuilder();
            while (data.next()) {
                array.add(Json.createObjectBuilder()
                        .add("namn", data.getString("namn"))
                        .add("antal0", data.getInt("intryck0"))
                        .add("antal1", data.getInt("intryck1"))
                        .add("antal2", data.getInt("intryck2"))
                        .build()
                );
            }

            conn.close();
            return array.build();

        } catch (Exception e) {
            System.out.println("LarareOmdomeManager");
            System.out.println(e.getMessage());
            return null;
        }

    }
}

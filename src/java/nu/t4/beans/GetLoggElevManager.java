/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nu.t4.beans;

import com.mysql.jdbc.Connection;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

/**
 *
 * @author Daniel Nilsson
 */
@Stateless
public class GetLoggElevManager {
    public JsonArray getLoggar(int elev_id){
        try {
            
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT loggbok.*, skolans_användare.namn"
                    + " FROM loggbok, skolans_användare WHERE loggbok.elev_id = "
                    + "skolans_användare.id AND loggbok.elev_id = %d ORDER BY loggbok.datum DESC", elev_id);
            ResultSet data = stmt.executeQuery(sql);
            JsonArrayBuilder jsonArray = Json.createArrayBuilder();
            while(data.next()){
                String stringIntryck ="";
                int intryck = data.getInt("intryck");
                if(intryck == 0){
                    stringIntryck = "dålig";
                }else if(intryck == 1){
                    stringIntryck = "sådär";
                }else if(intryck == 2){
                    stringIntryck = "bra";
                }else{
                    stringIntryck = "FEL";
                }
                jsonArray.add(Json.createObjectBuilder()
                        .add("ID", data.getInt("ID"))
                        .add("elev_id", data.getInt("elev_id"))
                        .add("innehall", data.getString("innehåll"))
                        .add("intryck", stringIntryck)
                        .add("datum", data.getString("datum"))
                        .add("bild_id", data.getInt("bild_id"))
                        .add("namn", data.getString("namn"))
                        .build());
            }
            conn.close();
            return jsonArray.build();
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}

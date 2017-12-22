package nu.t4.beans.global;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import nu.t4.beans.ConnectionFactory;

/**
 *
 * @author danlun2
 */
@Stateless
public class ProgramManager {

    //Hämtar alla program
    public JsonArray getProgram() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            String sql = String.format("SELECT * FROM program");
            Statement stmt = conn.createStatement();
            ResultSet data = stmt.executeQuery(sql);

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            while (data.next()) {
                arrayBuilder.add(
                        Json.createObjectBuilder()
                                .add("id", data.getInt("id"))
                                .add("namn", data.getString("namn"))
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
}

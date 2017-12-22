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
public class LarareHandledareManager {

    //Hämta alla handledare
    public JsonArray getHLNatverk() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = (Statement) conn.createStatement();

            //Hämtar information till handledarnätverket
            String sql = "SELECT * FROM handledareprogram";
            ResultSet data = stmt.executeQuery(sql);

            JsonArrayBuilder program = Json.createArrayBuilder();

            while (data.next()) {
                program.add(Json.createObjectBuilder()
                        .add("id", data.getInt("id"))
                        .add("handledare", data.getString("handledare"))
                        .add("telefonnummer", data.getString("telefonnummer"))
                        .add("email", data.getString("email"))
                        .add("namn", data.getString("namn"))//programnamn
                        .add("foretag", data.getString("foretag"))
                        .build());
            }

            conn.close();
            return program.build();

        } catch (Exception e) {
            System.out.println("elevhandledare - getHandledare()");
            System.out.println(e.getMessage());
            return null;
        }
    }
}

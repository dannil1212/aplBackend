package nu.t4.beans.larare;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import javax.ejb.Stateless;
import javax.json.JsonArray;
import javax.json.JsonObject;
import nu.t4.beans.ConnectionFactory;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author Daniel Nilsson
 */
@Stateless
public class LarareRedigeraAnvManager {

    //Spara ändringar i elev
    public boolean redigeraElev(int id, String namn, String tfnr, String email,
            int klass, int handledar_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "UPDATE google_anvandare SET namn = '%s', "
                    + "telefonnummer = '%s', email = '%s', ";
            //Handledare kan vara null
            if (handledar_id > -1) {
                sql += "handledare_id = %d, ";
            } else {
                sql += "handledare_id = null, ";
            }
            sql += "klass = %d WHERE id = %d";

            if (handledar_id > -1) {
                sql = String.format(sql, namn, tfnr, email, handledar_id, klass, id);
            } else {
                sql = String.format(sql, namn, tfnr, email, klass, id);
            }

            stmt.executeUpdate(sql);
            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //Spara ändringar i handledare
    public boolean redigeraHandledare(int id, String namn, String tfnr,
            String email, String foretag, String anvandarnamn, String losenord) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("UPDATE handledare SET namn = '%s', "
                    + "telefonnummer = '%s', "
                    + "email = '%s', "
                    + "foretag = '%s', "
                    + "anvandarnamn = '%s' ",
                    namn, tfnr, email, foretag, anvandarnamn);
            //Om lösenordet ändrats, kryptera och spara det
            if (!losenord.equals("")) {
                String encrypted_losenord = BCrypt.hashpw(losenord, BCrypt.gensalt());
                sql += String.format(", losenord = '%s' ", encrypted_losenord);
            }
            sql += String.format("WHERE id = %d", id);
            stmt.executeUpdate(sql);

            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //Tilldela handledare till elever
    public boolean setElevHandledare(JsonArray array) {
        try {
            com.mysql.jdbc.Connection conn = ConnectionFactory.getConnection();
            com.mysql.jdbc.Statement stmt = (com.mysql.jdbc.Statement) conn.createStatement();
            String sqlbaseSetHandledare = "UPDATE google_anvandare SET handledare_id = %d WHERE id = %d;";
            String sqlbaseSetNull = "UPDATE google_anvandare SET handledare_id = null WHERE id = %d;";
            String sql = "";

            stmt.execute("START TRANSACTION;");
            try {
                Iterator iterator = array.iterator();
                //Byt alla till null
                while (iterator.hasNext()) {
                    JsonObject item = (JsonObject) iterator.next();
                    int e_id = item.getInt("elev_id");
                    sql = String.format(sqlbaseSetNull, e_id);
                    stmt.addBatch(sql);
                }
                stmt.executeBatch();
                iterator = array.iterator();
                //Byt alla till ny handledare
                while (iterator.hasNext()) {
                    JsonObject item = (JsonObject) iterator.next();
                    int e_id = item.getInt("elev_id");
                    if (!item.isNull("handledare_id")) {
                        int h_id = item.getInt("handledare_id");
                        sql = String.format(sqlbaseSetHandledare, h_id, e_id);
                    }
                    stmt.addBatch(sql);
                }
                stmt.executeBatch();
            } catch (Exception e) {
                //Om nått gått fel, gå tillbaka till hur det var innan
                System.out.println("ElevHandledare TRANSACTION Failed");
                stmt.execute("ROLLBACK;");
                conn.close();
                throw e;
            }
            stmt.execute("COMMIT;");
            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println("elevhandledare - setElevHandledare()");
            System.out.println(e.getMessage());
            return false;
        }
    }
}

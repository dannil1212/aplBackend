package nu.t4.beans;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author carlkonig
 */
public class ConnectionFactory {

    //Databas inloggning, lägg inte upp det riktiga lösenordet på github
    private static final String DATABASE_USER = "aplapp";
    private static final String DATABASE_PASS = "qwerty";

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        String url = "jdbc:mysql://apl.teknikum.it/aplapp";
        return (Connection) DriverManager
                .getConnection(url, DATABASE_USER, DATABASE_PASS);
    }

    public static Connection getConnection(String host) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        String url;
        if (host.equals("local")) {
            url = "jdbc:mysql://localhost/aplapp";
        } else {
            url = "jdbc:mysql://apl.teknikum.it/aplapp";
        }
        return (Connection) DriverManager
                .getConnection(url, DATABASE_USER, DATABASE_PASS);
    }
}

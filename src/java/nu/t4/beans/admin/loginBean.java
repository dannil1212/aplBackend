package nu.t4.beans.admin;

import com.mysql.jdbc.Connection;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Named;
import nu.t4.beans.ConnectionFactory;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author Daniel Nilsson
 */
@ManagedBean
@Named
@ApplicationScoped
public class loginBean implements Serializable {

    private String username;
    private String password;
    private boolean loggedIn;

    //Getters & Setters
    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    //Kolla inloggningen
    public String login() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT * FROM admin WHERE anvandarnamn = '%s'", username);
            ResultSet data = stmt.executeQuery(sql);

            data.next();//Ta ut första raden
            String hashedpwd = data.getString("losenord");
            if (BCrypt.checkpw(password, hashedpwd)) {
                conn.close();
                loggedIn = true;
                return "main"; //Om inloggningen stämmer skickas man till startsidan
            } else {
                conn.close();
                loggedIn = false; //Om inloggningen misslyckas stannar man kvar på index
                return "index";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            loggedIn = false;
            return "index";
        }
    }

    public String logout() {
        loggedIn = false;
        return "index";
    }
}

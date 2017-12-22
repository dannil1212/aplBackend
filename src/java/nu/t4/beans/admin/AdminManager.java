package nu.t4.beans.admin;

import com.mysql.jdbc.Connection;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import nu.t4.beans.ConnectionFactory;
import org.mindrot.jbcrypt.BCrypt;
import org.primefaces.context.RequestContext;

/**
 *
 * @author Daniel Nilsson
 */
@ManagedBean
@ApplicationScoped
public class AdminManager implements Serializable {

    //Mappen som har bilderna
    private final String pathToBilder = "C:\\inetpub\\wwwroot\\fileupload\\uploads";

    private String klassnamn;
    private String programnamn;
    private String programIdNamn;

    private List filteredRadGamla;
    private List ignoreradeGamla;
    private int gamlaListaTyp;
    private int gamlaMonths;

    //Filtreringsvariabler används för sökningarna
    private List filteredRadElever;
    private List filteredRadLarare;
    private List filteredRadHL;
    private List filteredUsers;
    private List filteredHL;
    private List filteredAnv;
    private List filteredLarare;
    private Users selectedUser;
    private Users selectedHL;

    //Getters och setters start    
    public List getFilteredRadGamla() {
        return filteredRadGamla;
    }

    public void setFilteredRadGamla(List filteredRadGamla) {
        this.filteredRadGamla = filteredRadGamla;
    }

    public List getFilteredRadElever() {
        return filteredRadElever;
    }

    public void setFilteredRadElever(List filteredRadElever) {
        this.filteredRadElever = filteredRadElever;
    }

    public List getFilteredRadLarare() {
        return filteredRadLarare;
    }

    public void setFilteredRadLarare(List filteredRadLarare) {
        this.filteredRadLarare = filteredRadLarare;
    }

    public List getFilteredRadHL() {
        return filteredRadHL;
    }

    public void setFilteredRadHL(List filteredRadHL) {
        this.filteredRadHL = filteredRadHL;
    }

    public List getFilteredHL() {
        return filteredHL;
    }

    public void setFilteredHL(List filteredHL) {
        this.filteredHL = filteredHL;
    }

    public List getFilteredAnv() {
        return filteredAnv;
    }

    public void setFilteredAnv(List filteredAnv) {
        this.filteredAnv = filteredAnv;
    }

    public Users getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(Users selectedUser) {
        this.selectedUser = selectedUser;
    }

    public Users getSelectedHL() {
        return selectedHL;
    }

    public void setSelectedHL(Users selectedHL) {
        this.selectedHL = selectedHL;
    }

    public List getFilteredLarare() {
        return filteredLarare;
    }

    public void setFilteredLarare(List filteredLarare) {
        this.filteredLarare = filteredLarare;
    }

    public void setFilteredUsers(List filteredUsers) {
        this.filteredUsers = filteredUsers;
    }

    public String getProgramIdNamn() {
        return programIdNamn;
    }

    public void setProgramIdNamn(String programIdNamn) {
        this.programIdNamn = programIdNamn;
    }

    public String getProgramnamn() {
        return programnamn;
    }

    public void setProgramnamn(String programnamn) {
        this.programnamn = programnamn;
    }

    public String getKlassnamn() {
        return klassnamn;
    }

    public void setKlassnamn(String klassnamn) {
        this.klassnamn = klassnamn;
    }

    public List getFilteredUsers() {
        return filteredUsers;
    }

    public int getGamlaListaTyp() {
        return gamlaListaTyp;
    }

    public void setGamlaListaTyp(int gamlaListaTyp) {
        this.gamlaListaTyp = gamlaListaTyp;
    }

    public int getGamlaMonths() {
        return gamlaMonths;
    }

    public void setGamlaMonths(int gamlaMonths) {
        this.gamlaMonths = gamlaMonths;
    }

    public List getIgnoreradeGamla() {
        if (ignoreradeGamla == null) {
            setIgnoreradeGamla(new ArrayList());
        }
        return ignoreradeGamla;
    }

    public void setIgnoreradeGamla(List ignoreradeGamla) {
        this.ignoreradeGamla = ignoreradeGamla;
    }

    //Getters och setters slut
    //Visa redigeringssidan och spara vald skolanvändare
    public String redigeraSkAnv(Users temp) {
        selectedUser = new Users();
        selectedUser = temp;
        return "redigeraSkAnv";
    }

    //Visa redigeringssidan och spara vald handledare
    public String redigeraHL(Users temp) {
        selectedHL = new Users();
        selectedHL = temp;
        return "redigeraHL";
    }

    //Lägg till vald användare till ignoreringslistan (i ta bort gamla)
    public void ignoreraGammal(Users user) {
        getIgnoreradeGamla().add(user);
        resetFilters();
    }

    //Sluta ignorera vald användare
    public void slutaIgnoreraGammal(int index) {
        if (getIgnoreradeGamla().toArray().length > index) {
            getIgnoreradeGamla().remove(index);
            resetFilters();
        }
    }

    //Sluta ignorera alla användare
    public void tomIgnorerade() {
        getIgnoreradeGamla().clear();
        resetFilters();
    }

    //Lägger till klassen i databasen
    public void addClass() {
        try {
            int programid = 0;
            programid = getProgramId(programIdNamn);
            //Måste ha ett namn & program
            if (!klassnamn.equals("") && programid != 0) {
                Connection conn = ConnectionFactory.getConnection();
                Statement stmt = conn.createStatement();
                String sql = String.format(
                        "INSERT INTO klass (namn, program_id) "
                        + "VALUES('%s', %d)",
                        klassnamn, programid);
                stmt.executeUpdate(sql);
                conn.close();
            }
            klassnamn = "";
            programIdNamn = "Välj klass";

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Hamtar alla klasser, Klassnamn + Programnamn
    public List getClasses() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM klass_program ORDER BY klassnamn";
            ResultSet data = stmt.executeQuery(sql);
            List classes = new ArrayList();
            while (data.next()) {
                String temp = data.getString("programnamn") + ", " + data.getString("klassnamn");
                classes.add(temp);
            }
            conn.close();
            return classes;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Tar bort vald klass
    public void removeClass(String namn) {
        try {
            String tempArray[];
            //"klassnamn, programnamn" -> ["klassnamn","programnamn"]
            tempArray = namn.split(", ");

            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("DELETE FROM klass WHERE namn ='%s'", tempArray[1]);
            stmt.executeUpdate(sql);
            conn.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Hämtar alla användare som inte har lärarbehörighet, ID & Namn & Email
    public List getUsers() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT id, namn, email FROM google_anvandare WHERE behorighet = 0";
            ResultSet data = stmt.executeQuery(sql);

            List users = new ArrayList();
            while (data.next()) {
                Users user = new Users();
                user.setId(data.getInt("id"));
                user.setEmail(data.getString("email"));
                user.setNamn(data.getString("namn"));
                users.add(user);
            }
            conn.close();
            return users;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Sätter behörigheten som lärare mha deras email
    public void setBehorighet(String email) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("UPDATE google_anvandare SET "
                    + "behorighet = 1, handledare_id = NULL WHERE email ='%s'",
                    email);
            stmt.executeUpdate(sql);
            conn.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        RequestContext requestContext = RequestContext.getCurrentInstance();
        requestContext.execute("PF('larareTable').filter()");
        requestContext.execute("PF('anvTable').filter()");
    }

    //Tar bort behörigheten som lärare mha deras email
    public void removeBehorighet(String email) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("UPDATE google_anvandare SET behorighet = 0 WHERE email ='%s'", email);
            stmt.executeUpdate(sql);
            conn.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        RequestContext requestContext = RequestContext.getCurrentInstance();
        requestContext.execute("PF('larareTable').filter()");
        requestContext.execute("PF('anvTable').filter()");
    }

    //Hämtar alla som har lärarbehörighet, ID & Namn & Email
    public List getLarare() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT id, namn, email FROM google_anvandare WHERE behorighet = 1";
            ResultSet data = stmt.executeQuery(sql);

            List larare = new ArrayList();
            while (data.next()) {
                Users user = new Users();
                user.setId(data.getInt("id"));
                user.setEmail(data.getString("email"));
                user.setNamn(data.getString("namn"));
                larare.add(user);
            }
            conn.close();
            return larare;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Hämtar alla handledare, ID & Namn & Företag
    public List getHandledare() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT id, namn, foretag FROM handledare";
            ResultSet data = stmt.executeQuery(sql);
            List<Users> handledare = new ArrayList();
            while (data.next()) {
                Users user = new Users();
                user.setId(data.getInt("id"));

                String namn_foretag = data.getString("namn") + " - " + data.getString("foretag");
                user.setNamn_foretag(namn_foretag);
                handledare.add(user);
            }
            conn.close();
            return handledare;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Hämtar alla program, ID & Namn
    public List getProgram() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT id, namn FROM program";
            ResultSet data = stmt.executeQuery(sql);
            List<Users> programs = new ArrayList();
            while (data.next()) {
                Users user = new Users();
                user.setId(data.getInt("id"));
                user.setEmail(data.getString("namn"));
                programs.add(user);
            }
            conn.close();
            return programs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Hämtar alla klasser, ID & Namn
    public List getKlasser() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT id, namn FROM klass";
            ResultSet data = stmt.executeQuery(sql);
            List<Users> handledare = new ArrayList();
            while (data.next()) {
                Users user = new Users();
                user.setId(data.getInt("id"));
                user.setEmail(data.getString("namn"));
                handledare.add(user);
            }
            conn.close();
            return handledare;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Lägger till ett nytt program
    public void addProgram() {
        try {
            //Måste ha ett namn
            if (!programnamn.equals("")) {
                programnamn = programnamn.trim();
                Connection conn = ConnectionFactory.getConnection();
                Statement stmt = conn.createStatement();
                String sql = String.format("INSERT INTO program (namn) "
                        + "VALUES ('%s')", programnamn);
                stmt.executeUpdate(sql);
                conn.close();
            }
            //Töm textfältet
            programnamn = "";
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    //Hämtar alla program, Namn
    public List getPrograms() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM program";
            ResultSet data = stmt.executeQuery(sql);
            List programs = new ArrayList();
            while (data.next()) {
                programs.add(data.getString("namn"));
            }
            conn.close();
            return programs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Tar bort programmet
    public void removeProgram(String namn) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("DELETE FROM program WHERE namn ='%s'", namn);
            stmt.executeUpdate(sql);
            conn.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Hämtar id på program med dess namn
    public int getProgramId(String namn) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT id FROM program WHERE namn = '%s'", namn);
            ResultSet data = stmt.executeQuery(sql);
            data.next();
            int programId = data.getInt("id");
            conn.close();
            return programId;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    //Hämtar alla elever + lärare
    //ID & Namn & Tfnr & Email & klass & HL id & HL namn & behörighet
    public List getSkolansAnvandare() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM google_anvandare_handledare ORDER BY namn";
            ResultSet data = stmt.executeQuery(sql);
            List<Users> google_anvandare = new ArrayList();
            while (data.next()) {
                Users temp = new Users();
                temp.setId(data.getInt("id"));
                temp.setNamn(data.getString("namn"));
                temp.setTfnr(data.getString("telefonnummer"));
                temp.setEmail(data.getString("email"));
                temp.setKlass(data.getInt("klass"));
                temp.setHl_id(data.getInt("handledare_id"));
                temp.setHl_namn(data.getString("hl_namn"));
                temp.setBehorighet(data.getInt("behorighet"));
                google_anvandare.add(temp);
            }
            conn.close();
            return google_anvandare;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Hämtar alla användare som ej har loggat in på minst 12 månader & ej har en klass
    public List getGamlaAnv() {
        try {
            //Minst 12 månader om användaren inte matat in ett högre nummer
            int months = 12;
            if (getGamlaMonths() > 12) {
                months = getGamlaMonths();
            }

            //Välj elever eller lärare
            int behorighet = 0;
            if (getGamlaListaTyp() == 1) {
                behorighet = 1;
            }

            //Undantag
            String ignored = "-1";
            if (ignoreradeGamla.toArray().length > 0) {
                ignored = usersToSqlArray(ignoreradeGamla);
            }

            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT id, namn, email, senast_inloggad, created FROM google_anvandare "
                    + "WHERE senast_inloggad < DATE_SUB(NOW(),INTERVAL 12 MONTH) " //just in case
                    + "AND senast_inloggad < DATE_SUB(NOW(),INTERVAL %d MONTH) "
                    + "AND klass IS NULL AND behorighet = %d "
                    + "AND id NOT IN (%s) "
                    + "ORDER BY senast_inloggad DESC", months, behorighet, ignored);

            ResultSet data = stmt.executeQuery(sql);
            List<Users> google_anvandare = new ArrayList();
            while (data.next()) {
                Users temp = new Users();
                int id = data.getInt("id");

                //Det bör inte finnas några ignorerade i resultatet
                //Lika bra att vara säker när det handlar om att ta bort användare
                if (getIgnoreradeGamla().contains(id)) {
                    continue;
                }

                temp.setId(id);
                temp.setNamn(data.getString("namn"));
                temp.setEmail(data.getString("email"));

                temp.setSenast_inloggad(
                        new SimpleDateFormat("yyyy-MM-dd").format(
                                data.getTimestamp("senast_inloggad")
                        )
                );
                temp.setCreated(
                        new SimpleDateFormat("yyyy-MM-dd").format(
                                data.getTimestamp("created")
                        )
                );
                google_anvandare.add(temp);
            }
            conn.close();
            return google_anvandare;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Hämtar handledare
    //ID & namn & tfnr & email & program id & användarnamn & företag
    public List getHandledareAnv() {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM handledare ORDER BY namn";
            ResultSet data = stmt.executeQuery(sql);
            List<Users> handledareList = new ArrayList();
            while (data.next()) {
                Users temp = new Users();
                temp.setId(data.getInt("id"));
                temp.setNamn(data.getString("namn"));
                temp.setTfnr(data.getString("telefonnummer"));
                temp.setEmail(data.getString("email"));
                temp.setProgram_id(data.getInt("program_id"));
                temp.setAnvnamn(data.getString("anvandarnamn"));
                temp.setForetag(data.getString("foretag"));

                handledareList.add(temp);
            }
            conn.close();
            return handledareList;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Spara ändringar i skolanvändare
    public String sparaSkAnv() {
        try {
            int id = selectedUser.getId();
            String namn = selectedUser.getNamn();
            String tfnr = selectedUser.getTfnr();
            String email = selectedUser.getEmail();
            int klass = selectedUser.getKlass();
            int hl_id = selectedUser.getHl_id();

            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "";
            //Handledare kan vara null
            if (hl_id == 0) {
                sql = String.format("UPDATE google_anvandare SET namn = '%s', "
                        + "telefonnummer = '%s', "
                        + "email = '%s', "
                        + "handledare_id = null, "
                        + "klass = %d "
                        + "WHERE id = %d", namn, tfnr, email, klass, id);
            } else {
                sql = String.format("UPDATE google_anvandare SET namn = '%s', "
                        + "telefonnummer = '%s', "
                        + "email = '%s', "
                        + "handledare_id = %d, "
                        + "klass = %d "
                        + "WHERE id = %d", namn, tfnr, email, hl_id, klass, id);
            }
            stmt.executeUpdate(sql);
            conn.close();
            return "redigeraSkStart";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "redigeraSkAnv";
        }
    }

    //Spara ändringar i handledare
    public String sparaHL() {
        try {
            int id = selectedHL.getId();
            String namn = selectedHL.getNamn();
            String tfnr = selectedHL.getTfnr();
            String email = selectedHL.getEmail();
            int p_id = selectedHL.getProgram_id();
            String foretag = selectedHL.getForetag();
            String anvnamn = selectedHL.getAnvnamn();
            String losenord = selectedHL.getLosenord().trim();

            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("UPDATE handledare SET namn = '%s', "
                    + "telefonnummer = '%s', "
                    + "email = '%s', "
                    + "foretag = '%s', "
                    + "program_id = %d, "
                    + "anvandarnamn = '%s' ",
                    namn, tfnr, email, foretag, p_id, anvnamn);
            //Om nytt lösenord skrivits in, kryptera & uppdatera det
            if (!losenord.equals("")) {
                String encrypted_losenord = BCrypt.hashpw(losenord, BCrypt.gensalt());
                sql += String.format(", losenord = '%s' ", encrypted_losenord);
            }
            sql += String.format("WHERE id = %d", id);
            stmt.executeUpdate(sql);
            conn.close();
            return "redigeraHLStart";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "redigeraHL";
        }
    }

    //Ta bort vald handledare
    public String raderaHandledare(int id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = String.format("DELETE FROM handledare WHERE id = %d", id);
            stmt.executeUpdate(sql);
            conn.close();
            resetFilters();
            return "raderaMain";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "raderaMain";
        }
    }

    //Ta bort vald elev eller lärare
    public String raderaGoogle(int id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();

            //Hämta bilderna från loggböckerna först om de finns
            String sql = String.format("SELECT bild FROM loggbok, google_anvandare "
                    + "WHERE google_anvandare.id = %d "
                    + "AND google_anvandare.id = loggbok.elev_id "
                    + "AND bild IS NOT NULL ", id);

            ResultSet data = stmt.executeQuery(sql);
            List bilder = new ArrayList<>();
            while (data.next()) {
                //Spara bilderna för borttagning
                bilder.add(data.getString("bild"));
            }
            //Ta bort användaren
            sql = String.format("DELETE FROM google_anvandare WHERE id = %d", id);
            stmt.executeUpdate(sql);
            conn.close();
            //Om allt fungerat, ta bort bilderna
            raderaBilder(bilder);
            resetFilters();
            return "raderaMain";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "raderaMain";
        }
    }

    //Ta bort gamla användare
    public void raderaGamlaAnv() {
        try {
            int months = 12;
            if (getGamlaMonths() > 12) {
                months = getGamlaMonths();
            }

            int behorighet = 0;
            if (getGamlaListaTyp() == 1) {
                behorighet = 1;
            }

            String ignored = "-1";
            if (ignoreradeGamla.toArray().length > 0) {
                ignored = usersToSqlArray(ignoreradeGamla);
            }

            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute("START TRANSACTION");

            //Hämta bilderna först
            String sql = String.format("SELECT bild FROM loggbok, google_anvandare "
                    + "WHERE senast_inloggad < DATE_SUB(NOW(),INTERVAL 12 MONTH) " //just in case
                    + "AND senast_inloggad < DATE_SUB(NOW(),INTERVAL %d MONTH) "
                    + "AND klass IS NULL AND behorighet = %d "
                    + "AND google_anvandare.id NOT IN (%s) "
                    + "AND google_anvandare.id = loggbok.elev_id "
                    + "AND bild IS NOT NULL "
                    + "ORDER BY senast_inloggad DESC", months, behorighet, ignored);

            ResultSet data = stmt.executeQuery(sql);
            List bilder = new ArrayList<>();
            while (data.next()) {
                //Spara för borttagning
                bilder.add(data.getString("bild"));
            }

            //Ta bort användarna
            sql = String.format("DELETE FROM google_anvandare "
                    + "WHERE senast_inloggad < DATE_SUB(NOW(),INTERVAL 12 MONTH) " //just in case
                    + "AND senast_inloggad < DATE_SUB(NOW(),INTERVAL %d MONTH) "
                    + "AND klass IS NULL AND behorighet = %d "
                    + "AND id NOT IN (%s) "
                    + "ORDER BY senast_inloggad DESC", months, behorighet, ignored);

            stmt.executeUpdate(sql);
            stmt.execute("COMMIT");
            conn.close();
            //Om allt fungerat, ta bort bilderna
            raderaBilder(bilder);
            resetFilters();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Tar bort bilder
    private void raderaBilder(List<String> bilder) throws IOException {
        for (int i = 0; i < bilder.size(); i++) {
            //Filnamn på bilden
            String bild = bilder.get(i).replaceAll("\"", "");
            //Path till bilden
            Path fullPath = FileSystems.getDefault().getPath(pathToBilder, bild);
            //Ta bort om filen finns
            Files.deleteIfExists(fullPath);
        }
    }

    //Rensa sökningar
    public void resetFilters() {
        setFilteredRadGamla(getGamlaAnv());
        setFilteredAnv(getSkolansAnvandare());
        setFilteredHL(getHandledareAnv());
        setFilteredRadHL(getHandledareAnv());
        setFilteredRadLarare(getLarare());
        setFilteredRadElever(getUsers());
        RequestContext requestContext = RequestContext.getCurrentInstance();
        requestContext.execute("PF('anvTable').filter()");
    }

    //Rensa sökningar
    public void resetLarareFilter() {
        setFilteredLarare(getLarare());
        setFilteredUsers(getUsers());
        RequestContext requestContext = RequestContext.getCurrentInstance();
        requestContext.execute("PF('larareTable').filter()");
        requestContext.execute("PF('userTable').filter()");
    }

    //Tar ID från varje User i en lista & lägger ihop dem med ", "
    private static String usersToSqlArray(List<Users> users) {
        StringBuilder sb = new StringBuilder();
        boolean doneOne = false;
        for (Users user : users) {
            if (doneOne) {
                sb.append(", ");
            }
            sb.append(user.getId());
            doneOne = true;
        }
        return sb.toString();
    }
}

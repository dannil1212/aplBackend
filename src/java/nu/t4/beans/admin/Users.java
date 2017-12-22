package nu.t4.beans.admin;

/**
 *
 * @author Daniel Nilsson
 */
public class Users {

    private int id;
    private String namn;
    private String tfnr;
    private String email;
    private int klass;
    private int hl_id;
    private String hl_namn;
    private int behorighet;
    private String foretag;
    private int program_id;
    private String anvnamn;
    private String losenord;
    private String namn_foretag;
    private String senast_inloggad;
    private String created;

    //Getters & Setters
    public String getForetag() {
        return foretag;
    }

    public void setForetag(String foretag) {
        this.foretag = foretag;
    }

    public int getProgram_id() {
        return program_id;
    }

    public void setProgram_id(int program_id) {
        this.program_id = program_id;
    }

    public String getAnvnamn() {
        return anvnamn;
    }

    public void setAnvnamn(String anvnamn) {
        this.anvnamn = anvnamn;
    }

    public String getLosenord() {
        return losenord;
    }

    public void setLosenord(String losenord) {
        this.losenord = losenord;
    }

    public String getNamn_foretag() {
        return namn_foretag;
    }

    public void setNamn_foretag(String namn_foretag) {
        this.namn_foretag = namn_foretag;
    }

    public String getHl_namn() {
        return hl_namn;
    }

    public void setHl_namn(String hl_namn) {
        this.hl_namn = hl_namn;
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getBehorighet() {
        return behorighet;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTfnr() {
        return tfnr;
    }

    public void setTfnr(String tfnr) {
        this.tfnr = tfnr;
    }

    public int getKlass() {
        return klass;
    }

    public void setKlass(int klass) {
        this.klass = klass;
    }

    public int getHl_id() {
        return hl_id;
    }

    public void setHl_id(int hl_id) {
        this.hl_id = hl_id;
    }

    public void setBehorighet(int behorighet) {
        this.behorighet = behorighet;
    }

    public String getSenast_inloggad() {
        return senast_inloggad;
    }

    public void setSenast_inloggad(String senast_inloggad) {
        this.senast_inloggad = senast_inloggad;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}

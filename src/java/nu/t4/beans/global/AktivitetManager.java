package nu.t4.beans.global;

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
public class AktivitetManager {

    public final int NARVARO = 0;
    public final int LOGGBOK = 1;
    public final int MOMENT = 2;

    public final int HANDLEDARE = 0;
    public final int NEKADE = 1;

    //Hämta aktiviteter (handledare / elev)
    public JsonArray getAktiviteter(int anv_id, int tabell) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "";
            switch (tabell) {
                case HANDLEDARE:
                    sql = String.format("SELECT * FROM aktiviteter "
                            + "WHERE anvandar_id IN (SELECT id FROM google_anvandare "
                            + "WHERE handledare_id = %d) ORDER BY anvandar_id", anv_id);
                    break;
                case NEKADE:
                    sql = String.format("SELECT * FROM nekade_aktiviteter "
                            + "WHERE anvandar_id = %d", anv_id);
                    break;
                default:
                    break;
            }
            ResultSet data = stmt.executeQuery(sql);
            JsonArrayBuilder aktivitetListGroupBuilder = Json.createArrayBuilder();
            JsonArrayBuilder aktivitetListBuilder = Json.createArrayBuilder();
            int lastID = -1;
            while (data.next()) {
                //Gruppera handledarens aktiviteter mha elevernas id
                int newID = data.getInt("anvandar_id");
                if (lastID == -1) {
                    lastID = newID;
                } else if (lastID != newID) {
                    //Ny elev, bygg den förra eleven's aktiviteter & börja en ny
                    aktivitetListGroupBuilder.add(aktivitetListBuilder.build());
                    aktivitetListBuilder = Json.createArrayBuilder();
                }
                JsonObjectBuilder obuilder = Json.createObjectBuilder();
                int typ = data.getInt("typ");
                obuilder.add("typ", typ);
                int id = data.getInt("id");
                obuilder.add("id", id);
                obuilder.add("elev_id", newID);

                //Eftersom många kolumner kan vara (och är) null så måste det
                //hanteras genom att göra dem till Json null
                String innehall = data.getString("innehall");
                if (data.wasNull()) {
                    obuilder.add("innehall", JsonObject.NULL);
                } else {
                    obuilder.add("innehall", innehall);
                }
                int trafikljus = data.getInt("trafikljus");
                if (data.wasNull()) {
                    obuilder.add("trafikljus", JsonObject.NULL);
                } else {
                    obuilder.add("trafikljus", trafikljus);
                }
                String datum = data.getString("datum");
                if (data.wasNull()) {
                    obuilder.add("datum", JsonObject.NULL);
                } else {
                    obuilder.add("datum", datum);
                }
                String bild = data.getString("bild");
                if (data.wasNull()) {
                    obuilder.add("bild", JsonObject.NULL);
                } else {
                    obuilder.add("bild", bild);
                }
                aktivitetListBuilder.add(obuilder.build());
            }
            aktivitetListGroupBuilder.add(aktivitetListBuilder.build());
            conn.close();
            return aktivitetListGroupBuilder.build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Uppdatera handledare aktivitet, godkänd/nekad
    public boolean uppdateraAktivitet(int typ, int godkand, int aktivitets_id, int handledare_id) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "";
            switch (typ) {
                case NARVARO:
                    sql = String.format("UPDATE narvaro SET godkand = %d "
                            + "WHERE narvaro_id = %d AND anvandar_id IN (SELECT id FROM google_anvandare "
                            + "WHERE handledare_id = %d)", godkand, aktivitets_id, handledare_id);
                    break;
                case LOGGBOK:
                    sql = String.format("UPDATE loggbok SET godkand = %d "
                            + "WHERE id = %d AND elev_id IN (SELECT id FROM google_anvandare "
                            + "WHERE handledare_id = %d)", godkand, aktivitets_id, handledare_id);
                    break;
                case MOMENT:
                    godkand++;
                    sql = String.format("UPDATE koppla_moment_elev SET godkand = %d "
                            + "WHERE moment_id = %d AND anvandar_id IN (SELECT id FROM google_anvandare "
                            + "WHERE handledare_id = %d)", godkand, aktivitets_id, handledare_id);
                    break;
                default:
                    break;
            }
            stmt.executeUpdate(sql);
            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //Uppdatera elev aktivitet, färg för närvaro, innehåll för loggbok
    public boolean uppdateraElevAktivitet(int typ, int aktivitets_id, int elev_id, int trafikljus, String innehall) {
        try {
            Connection conn = ConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "";
            switch (typ) {
                case NARVARO:
                    sql = String.format("UPDATE narvaro SET godkand = 0, trafikljus = %d "
                            + "WHERE narvaro_id = %d AND anvandar_id = %d", trafikljus, aktivitets_id, elev_id);
                    break;
                case LOGGBOK:
                    sql = String.format("UPDATE loggbok SET godkand = 0, innehall = '%s'"
                            + "WHERE id = %d AND elev_id = %d", innehall, aktivitets_id, elev_id);
                    break;
                case MOMENT:
                    sql = String.format("UPDATE koppla_moment_elev SET godkand = 0 "
                            + "WHERE moment_id = %d AND anvandar_id = %d", aktivitets_id, elev_id);
                    break;
                default:
                    break;
            }
            stmt.executeUpdate(sql);
            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

}

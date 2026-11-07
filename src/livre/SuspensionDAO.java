package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SuspensionDAO {

    public boolean adherentEstSuspendu(int idAdherent) {
        String sql =
                "SELECT COUNT(*) FROM suspension "
              + "WHERE id_adherent = ? "
              + "AND date_debut <= NOW() "
              + "AND date_fin >= NOW()";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setInt(1, idAdherent);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean ajouterSuspension(int idAdherent, int idUtilisateur, String motif, String dateDebut, String dateFin) {
        if (idAdherent <= 0 || idUtilisateur <= 0 || motif == null || motif.trim().isEmpty()) {
            return false;
        }

        String sql =
                "INSERT INTO suspension (id_adherent, id_utilisateur, motif, date_debut, date_fin) "
              + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setInt(1, idAdherent);
            ps.setInt(2, idUtilisateur);
            ps.setString(3, motif);
            ps.setString(4, dateDebut);
            ps.setString(5, dateFin);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean leverSuspension(int idAdherent) {
        String sql =
                "UPDATE suspension "
              + "SET date_fin = NOW() "
              + "WHERE id_adherent = ? "
              + "AND date_fin >= NOW()";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setInt(1, idAdherent);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SuspensionDAO {

    public boolean estRoleAutorise(int idUtilisateur) {
        String sql =
                "SELECT COUNT(*) FROM utilisateur "
              + "WHERE id_utilisateur = ? "
              + "AND role IN ('administrateur', 'bibliothecaire')";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean estRoleAutoriseInterne(int idUtilisateur) {
        String sql =
                "SELECT COUNT(*) FROM utilisateur "
              + "WHERE id_utilisateur = ? "
              + "AND role IN ('administrateur', 'bibliothecaire')";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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

        if (!estRoleAutoriseInterne(idUtilisateur)) {
            return false;
        }

        if (dateDebut == null || dateFin == null || dateDebut.trim().isEmpty() || dateFin.trim().isEmpty()) {
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

    public boolean leverSuspension(int idAdherent, int idUtilisateur) {
        if (idAdherent <= 0 || idUtilisateur <= 0) {
            return false;
        }

        if (!estRoleAutoriseInterne(idUtilisateur)) {
            return false;
        }

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

    public boolean leverSuspension(int idAdherent) {
        return leverSuspension(idAdherent, 1);
    }

    public boolean appliquerSuspensionsAutomatiquesPourRetards() {
        String sqlRetards =
                "SELECT DISTINCT e.id_adherent, "
              + "DATEDIFF(CURDATE(), e.date_retour_prevue) AS jours_retard "
              + "FROM emprunt e "
              + "WHERE e.date_retour_reelle IS NULL "
              + "AND e.date_retour_prevue < CURDATE()";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sqlRetards);
             ResultSet rs = ps.executeQuery()) {

            boolean ok = true;
            while (rs.next()) {
                int idAdherent = rs.getInt("id_adherent");
                int joursRetard = rs.getInt("jours_retard");

                if (adherentEstSuspendu(idAdherent)) {
                    continue;
                }

                if (joursRetard >= 30) {
                    if (!ajouterSuspensionAuto(idAdherent, 60, "Retard de 1 mois ou plus : suspension automatique de 2 mois.")) {
                        ok = false;
                    }
                } else if (joursRetard >= 3) {
                    if (!ajouterSuspensionAuto(idAdherent, 30, "Retard de 3 jours : suspension automatique de 1 mois.")) {
                        ok = false;
                    }
                }
            }
            return ok;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean ajouterSuspensionAuto(int idAdherent, int dureeJours, String motif) {
        if (idAdherent <= 0 || dureeJours <= 0 || motif == null || motif.trim().isEmpty()) {
            return false;
        }

        String sql =
                "INSERT INTO suspension (id_adherent, id_utilisateur, motif, date_debut, date_fin) "
              + "VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? DAY))";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setInt(1, idAdherent);
            ps.setInt(2, 1);
            ps.setString(3, motif);
            ps.setInt(4, dureeJours);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
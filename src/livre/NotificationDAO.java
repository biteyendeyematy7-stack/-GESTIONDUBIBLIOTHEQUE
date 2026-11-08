package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NotificationDAO {

    public boolean ajouterNotification(int idAdherent, String typeNotification, String message) {
        if (idAdherent <= 0 || typeNotification == null || typeNotification.trim().isEmpty() || message == null || message.trim().isEmpty()) {
            return false;
        }

        String sqlExiste =
                "SELECT COUNT(*) FROM notification "
              + "WHERE id_adherent = ? "
              + "AND type_notification = ? "
              + "AND message = ? "
              + "AND DATE(date_envoi) = CURDATE()";

        try (Connection connexion = Connexion.getConnection()) {
            try (PreparedStatement psExiste = connexion.prepareStatement(sqlExiste)) {
                psExiste.setInt(1, idAdherent);
                psExiste.setString(2, typeNotification);
                psExiste.setString(3, message);
                try (ResultSet rs = psExiste.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return true;
                    }
                }
            }

            String sql =
                    "INSERT INTO notification (id_adherent, type_notification, message, date_envoi, lu) "
                  + "VALUES (?, ?, ?, NOW(), 0)";

            try (PreparedStatement ps = connexion.prepareStatement(sql)) {
                ps.setInt(1, idAdherent);
                ps.setString(2, typeNotification);
                ps.setString(3, message);
                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean ajouterNotificationDemande(int idAdherent, int idDemande, String typeNotification, String message) {
        if (idAdherent <= 0 || idDemande <= 0 || typeNotification == null || typeNotification.trim().isEmpty()) {
            return false;
        }

        String sql =
                "INSERT INTO notification (id_adherent, id_demande, type_notification, message, date_envoi, lu) "
              + "VALUES (?, ?, ?, ?, NOW(), 0)";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setInt(1, idAdherent);
            ps.setInt(2, idDemande);
            ps.setString(3, typeNotification);
            ps.setString(4, message);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean ajouterNotificationReservation(int idAdherent, int idReservation, String typeNotification, String message) {
        if (idAdherent <= 0 || idReservation <= 0 || typeNotification == null || typeNotification.trim().isEmpty()) {
            return false;
        }

        String sql =
                "INSERT INTO notification (id_adherent, id_reservation, type_notification, message, date_envoi, lu) "
              + "VALUES (?, ?, ?, ?, NOW(), 0)";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setInt(1, idAdherent);
            ps.setInt(2, idReservation);
            ps.setString(3, typeNotification);
            ps.setString(4, message);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean envoyerNotificationsAutomatiques() {
        boolean rappels = envoyerRappelsAvantRetour(2);
        boolean retards = notifierRetards();
        new SuspensionDAO().appliquerSuspensionsAutomatiquesPourRetards();
        return rappels || retards;
    }

    public boolean envoyerRappelsAvantRetour(int joursAvant) {
        if (joursAvant < 0) {
            return false;
        }

        String sql =
                "SELECT e.id_emprunt, e.id_adherent, e.date_retour_prevue "
              + "FROM emprunt e "
              + "WHERE e.date_retour_reelle IS NULL "
              + "AND e.date_retour_prevue BETWEEN DATE_ADD(CURDATE(), INTERVAL 0 DAY) "
              + "AND DATE_ADD(CURDATE(), INTERVAL ? DAY)";

        try (Connection connexion = Connexion.getConnection()) {
            try (PreparedStatement ps = connexion.prepareStatement(sql)) {
                ps.setInt(1, joursAvant);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean ok = true;
                    while (rs.next()) {
                        int idAdherent = rs.getInt("id_adherent");
                        String message = "Rappel : le retour du livre est prévu le "
                                + rs.getString("date_retour_prevue")
                                + ". Merci de le rapporter à la bibliothèque.";
                        if (!ajouterNotification(idAdherent, "rappel_retour", message)) {
                            ok = false;
                        }
                    }
                    return ok;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean notifierRetards() {
        String sql =
                "SELECT e.id_emprunt, e.id_adherent, e.date_retour_prevue "
              + "FROM emprunt e "
              + "WHERE e.date_retour_reelle IS NULL "
              + "AND e.date_retour_prevue < CURDATE()";

        try (Connection connexion = Connexion.getConnection()) {
            try (PreparedStatement ps = connexion.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                boolean ok = true;
                while (rs.next()) {
                    int idAdherent = rs.getInt("id_adherent");
                    String message = "Retard : le livre devait être rendu le "
                            + rs.getString("date_retour_prevue")
                            + ". Merci de le rapporter dès que possible.";
                    if (!ajouterNotification(idAdherent, "retard_retour", message)) {
                        ok = false;
                    }
                }
                return ok;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
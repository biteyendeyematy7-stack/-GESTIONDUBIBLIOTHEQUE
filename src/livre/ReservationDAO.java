package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReservationDAO {

    public boolean reserver(int idLivre, int idAdherent) {
        if (idLivre <= 0 || idAdherent <= 0) {
            return false;
        }

        SuspensionDAO suspensionDAO = new SuspensionDAO();
        if (suspensionDAO.adherentEstSuspendu(idAdherent)) {
            return false;
        }

        String sqlLivreIndisponible =
                "SELECT COUNT(*) FROM livre WHERE id_livre = ? AND nombre_disponible = 0";

        String sqlCheckAdherent =
                "SELECT COUNT(*) FROM adherent WHERE id_adherent = ?";

        String sqlCheckDoublon =
                "SELECT COUNT(*) FROM reservation "
              + "WHERE id_livre = ? AND id_adherent = ? "
              + "AND statut IN ('en_attente', 'disponible_a_retirer', 'remise')";

        String sqlCheckDemandeActive =
                "SELECT COUNT(*) FROM demande_emprunt "
              + "WHERE id_livre = ? AND id_adherent = ? "
              + "AND statut IN ('en_attente', 'acceptee_a_retirer', 'remise')";

        String sqlInsert =
                "INSERT INTO reservation (id_livre, id_adherent, date_reservation, statut) "
              + "VALUES (?, ?, NOW(), 'en_attente')";

        try (Connection connexion = Connexion.getConnection()) {

            try (PreparedStatement psLivre = connexion.prepareStatement(sqlLivreIndisponible)) {
                psLivre.setInt(1, idLivre);
                ResultSet rs = psLivre.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    return false;
                }
            }

            try (PreparedStatement psCheckAdherent = connexion.prepareStatement(sqlCheckAdherent)) {
                psCheckAdherent.setInt(1, idAdherent);
                ResultSet rsAdherent = psCheckAdherent.executeQuery();
                if (rsAdherent.next() && rsAdherent.getInt(1) == 0) {
                    return false;
                }
            }

            try (PreparedStatement psCheckDoublon = connexion.prepareStatement(sqlCheckDoublon)) {
                psCheckDoublon.setInt(1, idLivre);
                psCheckDoublon.setInt(2, idAdherent);
                ResultSet rsDoublon = psCheckDoublon.executeQuery();
                if (rsDoublon.next() && rsDoublon.getInt(1) > 0) {
                    return false;
                }
            }

            try (PreparedStatement psCheckDemande = connexion.prepareStatement(sqlCheckDemandeActive)) {
                psCheckDemande.setInt(1, idLivre);
                psCheckDemande.setInt(2, idAdherent);
                ResultSet rsDemande = psCheckDemande.executeQuery();
                if (rsDemande.next() && rsDemande.getInt(1) > 0) {
                    return false;
                }
            }

            try (PreparedStatement psInsert = connexion.prepareStatement(sqlInsert)) {
                psInsert.setInt(1, idLivre);
                psInsert.setInt(2, idAdherent);
                return psInsert.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean notifierProchaineReservation(int idLivre) {
        try (Connection connexion = Connexion.getConnection()) {
            connexion.setAutoCommit(false);

            try {
                boolean resultat = notifierProchaineReservation(connexion, idLivre);
                connexion.commit();
                return resultat;
            } catch (SQLException e) {
                connexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean notifierProchaineReservation(Connection connexion, int idLivre)
            throws SQLException {

        if (connexion == null || idLivre <= 0) {
            return false;
        }

        String sqlSelect =
                "SELECT id_reservation "
              + "FROM reservation "
              + "WHERE id_livre = ? AND statut = 'en_attente' "
              + "ORDER BY date_reservation ASC "
              + "LIMIT 1";

        String sqlMaj =
                "UPDATE reservation "
              + "SET statut = 'disponible_a_retirer', "
              + "    date_notification = NOW(), "
              + "    date_limite_retrait = DATE_ADD(NOW(), INTERVAL 2 DAY) "
              + "WHERE id_reservation = ? AND statut = 'en_attente'";

        int idReservation;

        try (PreparedStatement psSelect = connexion.prepareStatement(sqlSelect)) {
            psSelect.setInt(1, idLivre);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                idReservation = rs.getInt("id_reservation");
            }
        }

        try (PreparedStatement psMaj = connexion.prepareStatement(sqlMaj)) {
            psMaj.setInt(1, idReservation);
            if (psMaj.executeUpdate() == 0) {
                return false;
            }
        }

        CodeRetrait codeRetrait =
                new CodeRetraitDAO().genererCodePourReservation(
                        connexion,
                        idReservation,
                        2
                );

        if (codeRetrait == null) {
            throw new SQLException(
                    "Impossible de generer le code de retrait de la reservation."
            );
        }

        return true;
    }

    public boolean expirerReservationsNonRetirees() {
        String sql =
                "UPDATE reservation "
              + "SET statut = 'expiree' "
              + "WHERE statut = 'disponible_a_retirer' "
              + "AND date_remise IS NULL "
              + "AND date_limite_retrait IS NOT NULL "
              + "AND date_limite_retrait < NOW()";

        try (Connection connexion = Connexion.getConnection()) {
            try (PreparedStatement ps = connexion.prepareStatement(sql)) {
                return ps.executeUpdate() >= 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean confirmerRetraitReservation(int idReservation, int idUtilisateur) {
        String sqlSelect =
                "SELECT id_livre, id_adherent FROM reservation WHERE id_reservation = ? AND statut = 'disponible_a_retirer'";

        String sqlEmprunt =
                "INSERT INTO emprunt (id_livre, id_adherent, id_utilisateur, "
              + "date_emprunt, date_retour_prevue, date_retour_reelle, statut) "
              + "VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), NULL, 'en cours')";

        String sqlLivre =
                "UPDATE livre SET nombre_disponible = nombre_disponible - 1 "
              + "WHERE id_livre = ? AND nombre_disponible > 0";

        String sqlMajReservation =
                "UPDATE reservation "
              + "SET id_emprunt = LAST_INSERT_ID(), "
              + "    date_remise = NOW(), "
              + "    statut = 'remise' "
              + "WHERE id_reservation = ?";

        try (Connection connexion = Connexion.getConnection()) {
            connexion.setAutoCommit(false);

            try {
                int idLivre = 0;
                int idAdherent = 0;

                try (PreparedStatement psSelect = connexion.prepareStatement(sqlSelect)) {
                    psSelect.setInt(1, idReservation);
                    ResultSet rs = psSelect.executeQuery();

                    if (rs.next()) {
                        idLivre = rs.getInt("id_livre");
                        idAdherent = rs.getInt("id_adherent");
                    } else {
                        throw new SQLException("Réservation introuvable ou pas encore disponible à retirer.");
                    }
                }

                try (PreparedStatement psEmprunt = connexion.prepareStatement(sqlEmprunt)) {
                    psEmprunt.setInt(1, idLivre);
                    psEmprunt.setInt(2, idAdherent);
                    psEmprunt.setInt(3, idUtilisateur);

                    int nb = psEmprunt.executeUpdate();
                    if (nb == 0) {
                        throw new SQLException("Échec de création de l’emprunt.");
                    }
                }

                try (PreparedStatement psLivre = connexion.prepareStatement(sqlLivre)) {
                    psLivre.setInt(1, idLivre);
                    int nb = psLivre.executeUpdate();
                    if (nb == 0) {
                        throw new SQLException("Le livre n’est plus disponible.");
                    }
                }

                try (PreparedStatement psMaj = connexion.prepareStatement(sqlMajReservation)) {
                    psMaj.setInt(1, idReservation);
                    psMaj.executeUpdate();
                }

                connexion.commit();
                return true;

            } catch (SQLException e) {
                connexion.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean annulerReservation(int idReservation, int idAdherent) {
        if (idReservation <= 0 || idAdherent <= 0) {
            return false;
        }

        String sql =
                "DELETE FROM reservation "
              + "WHERE id_reservation = ? "
              + "AND id_adherent = ? "
              + "AND statut IN ('en_attente', 'disponible_a_retirer')";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idReservation);
            ps.setInt(2, idAdherent);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean reservationPeutEtreAnnulee(int idReservation, int idAdherent) {
        if (idReservation <= 0 || idAdherent <= 0) {
            return false;
        }

        String sql =
                "SELECT COUNT(*) FROM reservation "
              + "WHERE id_reservation = ? "
              + "AND id_adherent = ? "
              + "AND statut IN ('en_attente', 'disponible_a_retirer')";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idReservation);
            ps.setInt(2, idAdherent);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean annulerReservationBibliothecaire(int idReservation) {
        if (idReservation <= 0) {
            return false;
        }

        String sqlSelect =
                "SELECT id_livre FROM reservation "
              + "WHERE id_reservation = ? "
              + "AND statut IN ('en_attente', 'disponible_a_retirer')";

        String sqlDelete =
                "DELETE FROM reservation "
              + "WHERE id_reservation = ? "
              + "AND statut IN ('en_attente', 'disponible_a_retirer')";

        try (Connection connexion = Connexion.getConnection()) {
            connexion.setAutoCommit(false);

            try {
                int idLivre = -1;

                try (PreparedStatement psSelect = connexion.prepareStatement(sqlSelect)) {
                    psSelect.setInt(1, idReservation);
                    ResultSet rs = psSelect.executeQuery();

                    if (!rs.next()) {
                        throw new SQLException("Réservation introuvable ou non annulable.");
                    }
                    idLivre = rs.getInt("id_livre");
                }

                try (PreparedStatement psDelete = connexion.prepareStatement(sqlDelete)) {
                    psDelete.setInt(1, idReservation);
                    int rows = psDelete.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Impossible d’annuler cette réservation.");
                    }
                }

                String sqlWaiting =
                        "SELECT COUNT(*) FROM reservation "
                      + "WHERE id_livre = ? AND statut = 'en_attente'";

                try (PreparedStatement psWaiting = connexion.prepareStatement(sqlWaiting)) {
                    psWaiting.setInt(1, idLivre);
                    ResultSet rsWaiting = psWaiting.executeQuery();
                    if (rsWaiting.next() && rsWaiting.getInt(1) > 0) {
                        if (!notifierProchaineReservation(connexion, idLivre)) {
                            throw new SQLException(
                                    "Impossible de notifier la prochaine reservation."
                            );
                        }
                    }
                }

                connexion.commit();
                return true;

            } catch (SQLException e) {
                connexion.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

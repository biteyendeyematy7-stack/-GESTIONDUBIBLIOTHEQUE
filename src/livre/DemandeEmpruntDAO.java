package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DemandeEmpruntDAO {

    public boolean aUnEmpruntEnCours(int idLivre, int idAdherent) {
        if (idLivre <= 0 || idAdherent <= 0) {
            return false;
        }

        String sql =
                "SELECT COUNT(*) FROM emprunt "
              + "WHERE id_livre = ? AND id_adherent = ? AND date_retour_reelle IS NULL";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idLivre);
            ps.setInt(2, idAdherent);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean demander(int idLivre, int idAdherent) {
        if (idLivre <= 0 || idAdherent <= 0) {
            return false;
        }

        SuspensionDAO suspensionDAO = new SuspensionDAO();
        if (suspensionDAO.adherentEstSuspendu(idAdherent)) {
            return false;
        }

        String sqlEmpruntActif =
                "SELECT COUNT(*) FROM emprunt "
              + "WHERE id_livre = ? AND id_adherent = ? AND date_retour_reelle IS NULL";

        String sqlLivreDisponible =
                "SELECT COUNT(*) FROM livre WHERE id_livre = ? AND nombre_disponible > 0";

        String sqlDoublon =
                "SELECT COUNT(*) FROM demande_emprunt "
              + "WHERE id_livre = ? AND id_adherent = ? "
              + "AND statut IN ('en_attente', 'acceptee_a_retirer', 'remise')";

        String sqlReservationActive =
                "SELECT COUNT(*) FROM reservation "
              + "WHERE id_livre = ? AND id_adherent = ? "
              + "AND statut IN ('en_attente', 'disponible_a_retirer', 'remise')";

        String sqlInsert =
                "INSERT INTO demande_emprunt (id_livre, id_adherent, date_demande, statut) "
              + "VALUES (?, ?, NOW(), 'en_attente')";

        String sqlEmpruntsActifs =
                "SELECT COUNT(DISTINCT id_livre) FROM emprunt "
              + "WHERE id_adherent = ? AND date_retour_reelle IS NULL";

        try (Connection connexion = Connexion.getConnection()) {

            try (PreparedStatement psEmpruntsActifs = connexion.prepareStatement(sqlEmpruntsActifs)) {
                psEmpruntsActifs.setInt(1, idAdherent);
                ResultSet rs = psEmpruntsActifs.executeQuery();
                if (rs.next() && rs.getInt(1) >= 2) {
                    return false;
                }
            }

            try (PreparedStatement psEmpruntActif = connexion.prepareStatement(sqlEmpruntActif)) {
                psEmpruntActif.setInt(1, idLivre);
                psEmpruntActif.setInt(2, idAdherent);
                ResultSet rs = psEmpruntActif.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }

            try (PreparedStatement psLivre = connexion.prepareStatement(sqlLivreDisponible)) {
                psLivre.setInt(1, idLivre);
                ResultSet rs = psLivre.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    return false;
                }
            }

            try (PreparedStatement psDoublon = connexion.prepareStatement(sqlDoublon)) {
                psDoublon.setInt(1, idLivre);
                psDoublon.setInt(2, idAdherent);
                ResultSet rs = psDoublon.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }

            try (PreparedStatement psReservation = connexion.prepareStatement(sqlReservationActive)) {
                psReservation.setInt(1, idLivre);
                psReservation.setInt(2, idAdherent);
                ResultSet rs = psReservation.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
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

    public boolean accepter(int idDemande, int idUtilisateurDecision) {
        if (idDemande <= 0 || idUtilisateurDecision <= 0) {
            return false;
        }

        String sqlRole = "SELECT role FROM utilisateur WHERE id_utilisateur = ?";
        String sqlLivreDisponible =
                "SELECT d.id_livre, l.nombre_disponible FROM demande_emprunt d "
              + "INNER JOIN livre l ON l.id_livre = d.id_livre "
              + "WHERE d.id_demande = ? AND d.statut = 'en_attente'";

        String sqlReserverExemplaire =
                "UPDATE livre SET nombre_disponible = nombre_disponible - 1 "
              + "WHERE id_livre = ? AND nombre_disponible > 0";

        String sqlUpdate =
                "UPDATE demande_emprunt "
              + "SET statut = 'acceptee_a_retirer', "
              + "    id_utilisateur_decision = ?, "
              + "    date_decision = NOW(), "
              + "    date_limite_retrait = DATE_ADD(NOW(), INTERVAL 3 DAY) "
              + "WHERE id_demande = ? AND statut = 'en_attente'";

        try (Connection connexion = Connexion.getConnection()) {
            connexion.setAutoCommit(false);

            try {
                String role = null;
                try (PreparedStatement psRole = connexion.prepareStatement(sqlRole)) {
                    psRole.setInt(1, idUtilisateurDecision);
                    ResultSet rs = psRole.executeQuery();
                    if (rs.next()) {
                        role = rs.getString("role");
                    }
                }

                if (!"bibliothecaire".equals(role) && !"administrateur".equals(role)) {
                    return false;
                }

                int idLivre = 0;
                try (PreparedStatement psLivre = connexion.prepareStatement(sqlLivreDisponible)) {
                    psLivre.setInt(1, idDemande);
                    ResultSet rs = psLivre.executeQuery();
                    if (!rs.next()) {
                        return false;
                    }
                    idLivre = rs.getInt("id_livre");
                    if (rs.getInt("nombre_disponible") <= 0) {
                        return false;
                    }
                }

                try (PreparedStatement psReserver = connexion.prepareStatement(sqlReserverExemplaire)) {
                    psReserver.setInt(1, idLivre);
                    if (psReserver.executeUpdate() == 0) {
                        throw new SQLException("Pas d’exemplaire disponible à réserver.");
                    }
                }

                try (PreparedStatement psUpdate = connexion.prepareStatement(sqlUpdate)) {
                    psUpdate.setInt(1, idUtilisateurDecision);
                    psUpdate.setInt(2, idDemande);
                    if (psUpdate.executeUpdate() == 0) {
                        throw new SQLException("Demande introuvable ou déjà traitée.");
                    }
                }

                CodeRetrait codeRetrait =
                        new CodeRetraitDAO().genererCodePourDemande(
                                connexion,
                                idDemande,
                                3
                        );

                if (codeRetrait == null) {
                    throw new SQLException(
                            "Impossible de generer le code de retrait."
                    );
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

    public boolean refuser(int idDemande, int idUtilisateurDecision, String motif) {
        if (idDemande <= 0 || idUtilisateurDecision <= 0 || motif == null || motif.trim().isEmpty()) {
            return false;
        }

        String sqlRole = "SELECT role FROM utilisateur WHERE id_utilisateur = ?";
        String sqlUpdate =
                "UPDATE demande_emprunt "
              + "SET statut = 'refusee', "
              + "    id_utilisateur_decision = ?, "
              + "    date_decision = NOW(), "
              + "    motif_refus = ? "
              + "WHERE id_demande = ? AND statut = 'en_attente'";

        try (Connection connexion = Connexion.getConnection()) {
            String role = null;
            try (PreparedStatement psRole = connexion.prepareStatement(sqlRole)) {
                psRole.setInt(1, idUtilisateurDecision);
                ResultSet rs = psRole.executeQuery();
                if (rs.next()) {
                    role = rs.getString("role");
                }
            }

            if (!"bibliothecaire".equals(role) && !"administrateur".equals(role)) {
                return false;
            }

            try (PreparedStatement psUpdate = connexion.prepareStatement(sqlUpdate)) {
                psUpdate.setInt(1, idUtilisateurDecision);
                psUpdate.setString(2, motif);
                psUpdate.setInt(3, idDemande);
                return psUpdate.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean confirmerRetrait(int idDemande, int idUtilisateur) {
        if (idDemande <= 0 || idUtilisateur <= 0) {
            return false;
        }

        String sqlSelect =
                "SELECT id_livre, id_adherent FROM demande_emprunt WHERE id_demande = ? AND statut = 'acceptee_a_retirer'";

        String sqlEmprunt =
                "INSERT INTO emprunt (id_livre, id_adherent, id_utilisateur, "
              + "date_emprunt, date_retour_prevue, date_retour_reelle, statut) "
              + "VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), NULL, 'en cours')";

        String sqlMajDemande =
                "UPDATE demande_emprunt "
              + "SET id_emprunt = LAST_INSERT_ID(), "
              + "    date_remise = NOW(), "
              + "    statut = 'remise' "
              + "WHERE id_demande = ? AND statut = 'acceptee_a_retirer'";

        try (Connection connexion = Connexion.getConnection()) {
            connexion.setAutoCommit(false);

            try {
                int idLivre = 0;
                int idAdherent = 0;

                try (PreparedStatement psSelect = connexion.prepareStatement(sqlSelect)) {
                    psSelect.setInt(1, idDemande);
                    ResultSet rs = psSelect.executeQuery();

                    if (rs.next()) {
                        idLivre = rs.getInt("id_livre");
                        idAdherent = rs.getInt("id_adherent");
                    } else {
                        throw new SQLException("Demande introuvable ou pas encore acceptée.");
                    }
                }

                try (PreparedStatement psEmprunt = connexion.prepareStatement(sqlEmprunt)) {
                    psEmprunt.setInt(1, idLivre);
                    psEmprunt.setInt(2, idAdherent);
                    psEmprunt.setInt(3, idUtilisateur);

                    int nb = psEmprunt.executeUpdate();
                    if (nb == 0) {
                        throw new SQLException("Échec de création de l'emprunt.");
                    }
                }

                try (PreparedStatement psMaj = connexion.prepareStatement(sqlMajDemande)) {
                    psMaj.setInt(1, idDemande);
                    if (psMaj.executeUpdate() == 0) {
                        throw new SQLException("La demande n’est plus dans l’état attendu.");
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

    public boolean estDemandeExiste(int idDemande) {
        String sql = "SELECT COUNT(*) FROM demande_emprunt WHERE id_demande = ?";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setInt(1, idDemande);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean expirerDemandesNonRetirees() {
        String sql =
                "UPDATE demande_emprunt "
              + "SET statut = 'expiree' "
              + "WHERE statut = 'acceptee_a_retirer' "
              + "AND date_remise IS NULL "
              + "AND date_limite_retrait IS NOT NULL "
              + "AND date_limite_retrait < NOW()";

        String sqlLibererExemplaire =
                "UPDATE livre l "
              + "INNER JOIN demande_emprunt d ON d.id_livre = l.id_livre "
              + "SET l.nombre_disponible = l.nombre_disponible + 1 "
              + "WHERE d.statut = 'expiree' "
              + "AND d.date_remise IS NULL "
              + "AND d.date_limite_retrait < NOW()";

        try (Connection connexion = Connexion.getConnection()) {
            connexion.setAutoCommit(false);
            try {
                try (PreparedStatement psUpdate = connexion.prepareStatement(sql)) {
                    psUpdate.executeUpdate();
                }

                try (PreparedStatement psLiberer = connexion.prepareStatement(sqlLibererExemplaire)) {
                    psLiberer.executeUpdate();
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

    public int compterEmpruntsActifsAdherent(int idAdherent) {
        if (idAdherent <= 0) {
            return 0;
        }

        String sql =
                "SELECT COUNT(DISTINCT id_livre) "
              + "FROM emprunt "
              + "WHERE id_adherent = ? AND date_retour_reelle IS NULL";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idAdherent);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean peutFaireNouvelEmprunt(int idAdherent) {
        return compterEmpruntsActifsAdherent(idAdherent) < 2;
    }

    public boolean annulerDemande(int idDemande, int idAdherent) {
        if (idDemande <= 0 || idAdherent <= 0) {
            return false;
        }

        String sql =
                "UPDATE demande_emprunt "
              + "SET statut = 'annulee' "
              + "WHERE id_demande = ? "
              + "AND id_adherent = ? "
              + "AND statut IN ('en_attente', 'acceptee_a_retirer')";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idDemande);
            ps.setInt(2, idAdherent);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean demandePeutEtreAnnulee(int idDemande, int idAdherent) {
        if (idDemande <= 0 || idAdherent <= 0) {
            return false;
        }

        String sql =
                "SELECT COUNT(*) FROM demande_emprunt "
              + "WHERE id_demande = ? AND id_adherent = ? "
              + "AND statut IN ('en_attente', 'acceptee_a_retirer')";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idDemande);
            ps.setInt(2, idAdherent);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

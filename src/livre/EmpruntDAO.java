package livre;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

public class EmpruntDAO {
	public boolean ajouterEmprunt(

	        int idLivre,
	        int idAdherent,
	        int idUtilisateur,
	        String dateEmprunt,
	        String dateRetourPrevue) {

	    String sqlRole =
	            "SELECT role FROM utilisateur "
	          + "WHERE id_utilisateur = ?";
	    

	    String sqlEmprunt =
	            "INSERT INTO emprunt "
	          + "(id_livre, id_adherent, id_utilisateur, "
	          + "date_emprunt, date_retour_prevue, statut) "
	          + "VALUES (?, ?, ?, ?, ?, 'en cours')";

	    String sqlLivre =
	            "UPDATE livre "
	          + "SET nombre_disponible = nombre_disponible - 1 "
	          + "WHERE id_livre = ? "
	          + "AND nombre_disponible > 0";

	    try (Connection connexion = Connexion.getConnection()) {

	        connexion.setAutoCommit(false);

	        try {

	            // Vérifier le rôle de l'utilisateur
	            String role = null;

	            try (PreparedStatement psRole =
	                    connexion.prepareStatement(sqlRole)) {

	                psRole.setInt(1, idUtilisateur);

	                ResultSet rs = psRole.executeQuery();

	                if (rs.next()) {

	                    role = rs.getString("role");

	                } else {

	                    throw new SQLException(
	                            "Utilisateur introuvable."
	                    );
	                }
	            }
	            

	            // Seul administrateur ou bibliothécaire peut enregistrer
	            if (!"administrateur".equals(role)
	                    && !"bibliothecaire".equals(role)) {

	                throw new SQLException(
	                        "Un adhérent ne peut pas enregistrer un emprunt."
	                );
	            }
	         // Vérifier si l'adhérent possède déjà ce livre
	            String sqlDoublon =
	                    "SELECT COUNT(*) FROM emprunt "
	                  + "WHERE id_adherent = ? "
	                  + "AND id_livre = ? "
	                  + "AND date_retour_reelle IS NULL";

	            try (PreparedStatement psDoublon =
	                    connexion.prepareStatement(sqlDoublon)) {

	                psDoublon.setInt(1, idAdherent);
	                psDoublon.setInt(2, idLivre);

	                ResultSet rsDoublon = psDoublon.executeQuery();

	                if (rsDoublon.next() && rsDoublon.getInt(1) > 0) {

	                    throw new RuntimeException(
	                        "Cet adhérent possède déjà ce livre. "
	                      + "Il doit d'abord retourner le premier emprunt."
	                    );
	                }
	            }

	            // Vérifier la disponibilité du livre
	            try (PreparedStatement psLivre =
	                    connexion.prepareStatement(sqlLivre)) {

	                psLivre.setInt(1, idLivre);

	                int resultat =
	                        psLivre.executeUpdate();

	                if (resultat == 0) {

	                    throw new SQLException(
	                            "Le livre n'est pas disponible."
	                    );
	                }
	            }

	            // Enregistrer l'emprunt
	            try (PreparedStatement psEmprunt =
	                    connexion.prepareStatement(sqlEmprunt)) {

	                psEmprunt.setInt(1, idLivre);
	                psEmprunt.setInt(2, idAdherent);
	                psEmprunt.setInt(3, idUtilisateur);
	                psEmprunt.setString(4, dateEmprunt);
	                psEmprunt.setString(5, dateRetourPrevue);

	                psEmprunt.executeUpdate();
	            }

	            connexion.commit();

	            System.out.println(
	                    "Emprunt enregistré avec succès !"
	            );

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
	public ResultSet afficherEmprunts() {

	    String sql =
	            "SELECT e.id_emprunt, "
	          + "l.titre, "
	          + "a.nom, "
	          + "a.prenom, "
	          + "e.date_emprunt, "
	          + "e.date_retour_prevue, "
	          + "e.date_retour_reelle, "
	          + "CASE "
	          + "WHEN e.date_retour_reelle IS NOT NULL "
	          + "THEN 'retourne' "
	          + "WHEN e.date_retour_prevue < CURDATE() "
	          + "THEN 'en retard' "
	          + "ELSE 'en cours' "
	          + "END AS statut "
	          + "FROM emprunt e "
	          + "INNER JOIN livre l "
	          + "ON e.id_livre = l.id_livre "
	          + "INNER JOIN adherent a "
	          + "ON e.id_adherent = a.id_adherent "
	          + "ORDER BY e.id_emprunt DESC";

	    try {

	        Connection connexion =
	                Connexion.getConnection();

	        PreparedStatement ps =
	                connexion.prepareStatement(sql);

	        return ps.executeQuery();

	    } catch (SQLException e) {

	        e.printStackTrace();
	        return null;
	    }
	}
    public boolean enregistrerRetour(
            int idEmprunt,
            String dateRetourReelle) {

        String sqlEmprunt =
                "UPDATE emprunt "
              + "SET date_retour_reelle = ?, "
              + "statut = 'retourne' "
              + "WHERE id_emprunt = ? "
              + "AND statut <> 'retourne'";

        String sqlLivre =
                "UPDATE livre l "
              + "INNER JOIN emprunt e "
              + "ON l.id_livre = e.id_livre "
              + "SET l.nombre_disponible = l.nombre_disponible + 1 "
              + "WHERE e.id_emprunt = ?";

        try (Connection connexion = Connexion.getConnection()) {

            connexion.setAutoCommit(false);

            try {

                // 1. Enregistrer le retour
                try (PreparedStatement psEmprunt =
                        connexion.prepareStatement(sqlEmprunt)) {

                    psEmprunt.setString(1, dateRetourReelle);
                    psEmprunt.setInt(2, idEmprunt);

                    int resultat = psEmprunt.executeUpdate();

                    if (resultat == 0) {
                        throw new SQLException(
                                "Emprunt introuvable ou déjà retourné."
                        );
                    }
                }

                // 2. Rendre l'exemplaire disponible
                try (PreparedStatement psLivre =
                        connexion.prepareStatement(sqlLivre)) {

                    psLivre.setInt(1, idEmprunt);

                    psLivre.executeUpdate();
                }

                connexion.commit();

                System.out.println(
                        "Retour enregistré avec succès !"
                );
                return true;

            } catch (SQLException e) {

                connexion.rollback();
                throw e;
            }

        } catch (SQLException e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Erreur : " + e.getMessage()
            );
        }
    }
    public Map<String, Integer> getLivresDisponibles() {

        Map<String, Integer> livres =
                new LinkedHashMap<>();

        String sql =
                "SELECT id_livre, titre, nombre_disponible "
              + "FROM livre "
              + "WHERE nombre_disponible > 0 "
              + "ORDER BY titre";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int id =
                        rs.getInt("id_livre");

                String titre =
                        rs.getString("titre");

                int disponible =
                        rs.getInt("nombre_disponible");

                String affichage =
                        titre + " (" + disponible
                        + " disponible"
                        + (disponible > 1 ? "s" : "")
                        + ")";

                livres.put(affichage, id);
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return livres;
    }
    public Map<String, Integer> getAdherents() {

        Map<String, Integer> adherents =
                new LinkedHashMap<>();

        String sql =
                "SELECT id_adherent, nom, prenom "
              + "FROM adherent "
              + "ORDER BY nom, prenom";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int id =
                        rs.getInt("id_adherent");

                String nom =
                        rs.getString("nom");

                String prenom =
                        rs.getString("prenom");

                String affichage =
                        nom + " " + prenom;

                adherents.put(affichage, id);
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return adherents;
    }
    public Map<String, Integer> getEmpruntsEnCours() {

        Map<String, Integer> emprunts =
                new LinkedHashMap<>();

        String sql =
                "SELECT e.id_emprunt, l.titre, "
              + "a.nom, a.prenom, e.date_retour_prevue "
              + "FROM emprunt e "
              + "INNER JOIN livre l "
              + "ON e.id_livre = l.id_livre "
              + "INNER JOIN adherent a "
              + "ON e.id_adherent = a.id_adherent "
              + "WHERE e.date_retour_reelle IS NULL "
              + "ORDER BY e.date_retour_prevue";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps =
                     connexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int id = rs.getInt("id_emprunt");

                String titre = rs.getString("titre");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String dateRetour =
                        rs.getString("date_retour_prevue");

                String affichage =
                        "#" + id
                        + " - " + titre
                        + " - " + nom + " " + prenom
                        + " - retour prévu : " + dateRetour;

                emprunts.put(affichage, id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return emprunts;
    }
    public boolean modifierEmpruntDates(
            int idEmprunt,
            String dateEmprunt,
            String dateRetourPrevue) {

        String sql =
                "UPDATE emprunt "
              + "SET date_emprunt = ?, "
              + "date_retour_prevue = ? "
              + "WHERE id_emprunt = ? "
              + "AND date_retour_reelle IS NULL";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setString(1, dateEmprunt);
            ps.setString(2, dateRetourPrevue);
            ps.setInt(3, idEmprunt);

            int resultat = ps.executeUpdate();

            if (resultat > 0) {
                System.out.println(
                        "Emprunt modifié avec succès !"
                );
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    public Map<String, Integer> getEmpruntsRetournes() {

        Map<String, Integer> emprunts = new java.util.LinkedHashMap<>();

        String sql =
                "SELECT e.id_emprunt, "
              + "l.titre, "
              + "a.nom, "
              + "a.prenom, "
              + "e.date_retour_prevue, "
              + "e.date_retour_reelle "
              + "FROM emprunt e "
              + "INNER JOIN livre l ON e.id_livre = l.id_livre "
              + "INNER JOIN adherent a ON e.id_adherent = a.id_adherent "
              + "WHERE e.date_retour_reelle IS NOT NULL "
              + "ORDER BY e.id_emprunt DESC";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String texte =
                        rs.getString("titre")
                        + " - "
                        + rs.getString("nom")
                        + " - "
                        + rs.getString("prenom")
                        + " - "
                        + rs.getString("date_retour_prevue")
                        + " - "
                        + rs.getString("date_retour_reelle");

                emprunts.put(
                        texte,
                        rs.getInt("id_emprunt")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return emprunts;
    }
    public boolean modifierDateRetour(
            int idEmprunt,
            String nouvelleDateRetour) {

        String sql =
                "UPDATE emprunt "
              + "SET date_retour_reelle = ?, "
              + "statut = 'retourne' "
              + "WHERE id_emprunt = ? "
              + "AND date_retour_reelle IS NOT NULL";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setString(1, nouvelleDateRetour);
            ps.setInt(2, idEmprunt);

            int resultat = ps.executeUpdate();

            if (resultat > 0) {
                System.out.println(
                        "Date de retour modifiée avec succès !"
                );
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    public long calculerRetardJours(int idEmprunt) {
        String sql =
                "SELECT date_retour_prevue, date_retour_reelle "
              + "FROM emprunt "
              + "WHERE id_emprunt = ?";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setInt(1, idEmprunt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String prevu = rs.getString("date_retour_prevue");
                    String reel = rs.getString("date_retour_reelle");
                    if (reel == null || prevu == null || reel.trim().isEmpty()) {
                        return 0;
                    }
                    LocalDate datePrevue = LocalDate.parse(prevu.substring(0, Math.min(10, prevu.length())));
                    LocalDate dateReelle = LocalDate.parse(reel.substring(0, Math.min(10, reel.length())));
                    return Math.max(0, ChronoUnit.DAYS.between(datePrevue, dateReelle));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean aUnRetourEnRetard(int idEmprunt) {
        return calculerRetardJours(idEmprunt) > 0;
    }
    public int getIdAdherentParEmprunt(int idEmprunt) {
        String sql =
                "SELECT id_adherent FROM emprunt WHERE id_emprunt = ?";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idEmprunt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_adherent");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getDateRetourPrevueParEmprunt(int idEmprunt) {
        String sql =
                "SELECT date_retour_prevue FROM emprunt WHERE id_emprunt = ?";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idEmprunt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("date_retour_prevue");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
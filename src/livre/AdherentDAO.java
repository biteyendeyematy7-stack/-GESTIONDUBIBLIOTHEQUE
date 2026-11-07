package livre;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdherentDAO {
	public void ajouterAdherent(
	        String nom,
	        String prenom,
	        String adresse,
	        String telephone,
	        String email,
	        String dateInscription,
	        String login
	        )
	        {

	    String motDePasseTemporaire = "bibliotheque";

	    String sqlUtilisateur =
	            "INSERT INTO utilisateur "
	          + "(nom, prenom, login, mot_de_passe, role, premiere_connexion) "
	          + "VALUES (?, ?, ?, ?, 'adherent', TRUE)";

	    String sqlAdherent =
	            "INSERT INTO adherent "
	          + "(id_utilisateur, nom, prenom, adresse, telephone, email, date_inscription) "
	          + "VALUES (?, ?, ?, ?, ?, ?, ?)";

	    try (Connection connexion = Connexion.getConnection()) {

	        if (connexion == null) {
	            System.out.println("Impossible de créer l'adhérent : base de données inaccessible.");
	            return;
        }

	        connexion.setAutoCommit(false);

	        try {

	            int idUtilisateur;

	            // 1. Création du compte utilisateur
	            try (PreparedStatement psUtilisateur =
	                    connexion.prepareStatement(
	                            sqlUtilisateur,
	                            PreparedStatement.RETURN_GENERATED_KEYS)) {

	                psUtilisateur.setString(1, nom);
	                psUtilisateur.setString(2, prenom);
	                psUtilisateur.setString(3, login);
	                psUtilisateur.setString(4, motDePasseTemporaire);

	                psUtilisateur.executeUpdate();

	                ResultSet rs =
	                        psUtilisateur.getGeneratedKeys();

	                if (!rs.next()) {

	                    throw new SQLException(
	                            "Impossible de récupérer l'ID utilisateur."
	                    );
                }

	                idUtilisateur = rs.getInt(1);
	            }

	            // 2. Création du profil adhérent
	            try (PreparedStatement psAdherent =
	                    connexion.prepareStatement(sqlAdherent)) {

	                psAdherent.setInt(1, idUtilisateur);
	                psAdherent.setString(2, nom);
	                psAdherent.setString(3, prenom);
	                psAdherent.setString(4, adresse);
	                psAdherent.setString(5, telephone);
	                psAdherent.setString(6, email);
	                psAdherent.setString(7, dateInscription);

	                psAdherent.executeUpdate();
	            }

	            // 3. Validation
	            connexion.commit();

	            System.out.println(
	                    "Compte adhérent créé avec succès !"
	            );

	            System.out.println(
	                    "Login de l'adhérent : " + login
	            );

	            System.out.println(
	                    "Mot de passe temporaire : " + motDePasseTemporaire
	            );

	        } catch (SQLException e) {

	            connexion.rollback();
	            throw e;
	        }

	    } catch (SQLException e) {

	        e.printStackTrace();
	    }
	}
	public void modifierAdherent(
	        int idAdherent,
	        String nom,
	        String prenom,
	        String adresse,
	        String telephone,
	        String email,
	        String dateInscription) {

	    String sql = "UPDATE adherent SET nom = ?, prenom = ?, "
	               + "adresse = ?, telephone = ?, email = ?, "
	               + "date_inscription = ? "
	               + "WHERE id_adherent = ?";

	    try (Connection connexion = Connexion.getConnection();
	         PreparedStatement ps = connexion.prepareStatement(sql)) {

	        ps.setString(1, nom);
	        ps.setString(2, prenom);
	        ps.setString(3, adresse);
	        ps.setString(4, telephone);
	        ps.setString(5, email);
	        ps.setString(6, dateInscription);
	        ps.setInt(7, idAdherent);

	        ps.executeUpdate();

	        System.out.println("Adhérent modifié avec succès !");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}public void supprimerAdherent(int idAdherent) {

	    String sql = "DELETE FROM adherent WHERE id_adherent = ?";

	    try (Connection connexion = Connexion.getConnection();
	         PreparedStatement ps = connexion.prepareStatement(sql)) {

	        ps.setInt(1, idAdherent);

	        ps.executeUpdate();

	        System.out.println("Adhérent supprimé avec succès !");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	public ResultSet rechercherAdherents(String recherche) {

	    String sql =
	            "SELECT a.id_adherent, "
	          + "a.nom, "
	          + "a.prenom, "
	          + "a.adresse, "
	          + "a.telephone, "
	          + "a.email, "
	          + "a.date_inscription, "
	          + "u.login "
	          + "FROM adherent a "
	          + "INNER JOIN utilisateur u "
	          + "ON a.id_utilisateur = u.id_utilisateur "
	          + "WHERE a.nom LIKE ? "
	          + "OR a.prenom LIKE ? "
	          + "OR a.telephone LIKE ?";

	    try {

	        Connection connexion =
	                Connexion.getConnection();

	        PreparedStatement ps =
	                connexion.prepareStatement(sql);

	        String texte = "%" + recherche + "%";

	        ps.setString(1, texte);
	        ps.setString(2, texte);
	        ps.setString(3, texte);

	        return ps.executeQuery();

	    } catch (SQLException e) {

	        e.printStackTrace();
	        return null;
	    }
	}

	public ResultSet afficherAdherents() {

	    String sql =
	            "SELECT a.id_adherent, a.nom, a.prenom, a.adresse, "
	          + "a.telephone, a.email, a.date_inscription, "
	          + "u.login "
	          + "FROM adherent a "
	          + "INNER JOIN utilisateur u "
	          + "ON a.id_utilisateur = u.id_utilisateur";

	    try {

	        Connection connexion = Connexion.getConnection();

	        PreparedStatement ps =
	                connexion.prepareStatement(sql);

	        return ps.executeQuery();

	    } catch (SQLException e) {

	        e.printStackTrace();
	        return null;
	    }
	}
	public int compterAdherents() {

	    String sql = "SELECT COUNT(*) FROM adherent";

	    try (Connection connexion = Connexion.getConnection();
	         PreparedStatement ps = connexion.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {

	        if (rs.next()) {
	            return rs.getInt(1);
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return 0;
	}

}
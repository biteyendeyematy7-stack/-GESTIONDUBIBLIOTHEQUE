package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtilisateurDAO {

    // =========================================================
    // Vérification classique du login et du mot de passe
    // =========================================================

    public static String verifierConnexion(
            String login,
            String motDePasse) {

        String sql =
                "SELECT role FROM utilisateur "
              + "WHERE login = ? AND mot_de_passe = ?";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps =
                     connexion.prepareStatement(sql)) {

            ps.setString(1, login);
            ps.setString(2, motDePasse);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("role");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // =========================================================
    // Connexion classique
    // =========================================================

    public static Utilisateur connecter(
            String login,
            String motDePasse) {

        String sql =
                "SELECT id_utilisateur, login, role "
              + "FROM utilisateur "
              + "WHERE login = ? AND mot_de_passe = ?";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps =
                     connexion.prepareStatement(sql)) {

            ps.setString(1, login);
            ps.setString(2, motDePasse);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("login"),
                        rs.getString("role")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // =========================================================
    // Recherche d'un utilisateur uniquement avec son login
    // Utilisée pour la première connexion d'un adhérent
    // =========================================================

    public static Utilisateur rechercherParLogin(String login) {

        String sql =
                "SELECT id_utilisateur, login, role "
              + "FROM utilisateur "
              + "WHERE login = ?";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps =
                     connexion.prepareStatement(sql)) {

            ps.setString(1, login);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new Utilisateur(
                        rs.getInt("id_utilisateur"),
                        rs.getString("login"),
                        rs.getString("role")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // =========================================================
    // Vérifier si l'utilisateur est en première connexion
    // =========================================================

    public static boolean estPremiereConnexion(int idUtilisateur) {

        String sql =
                "SELECT premiere_connexion "
              + "FROM utilisateur "
              + "WHERE id_utilisateur = ?";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps =
                     connexion.prepareStatement(sql)) {

            ps.setInt(1, idUtilisateur);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("premiere_connexion") == 1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    // =========================================================
    // Enregistrer le nouveau mot de passe
    // et terminer la première connexion
    // =========================================================

    public static boolean definirNouveauMotDePasse(
            int idUtilisateur,
            String nouveauMotDePasse) {

        String sql =
                "UPDATE utilisateur "
              + "SET mot_de_passe = ?, "
              + "premiere_connexion = 0 "
              + "WHERE id_utilisateur = ?";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps =
                     connexion.prepareStatement(sql)) {

            ps.setString(1, nouveauMotDePasse);
            ps.setInt(2, idUtilisateur);

            int resultat = ps.executeUpdate();

            return resultat > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    public static boolean reinitialiserMotDePasse(
            String login,
            String nom,
            String prenom,
            String nouveauMotDePasse) {

        String sql =
                "UPDATE utilisateur u "
              + "INNER JOIN adherent a "
              + "ON u.id_utilisateur = a.id_utilisateur "
              + "SET u.mot_de_passe = ?, "
              + "u.premiere_connexion = 0 "
              + "WHERE u.login = ? "
              + "AND u.nom = ? "
              + "AND u.prenom = ? "
              + "AND u.role = 'adherent'";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps =
                     connexion.prepareStatement(sql)) {

            ps.setString(1, nouveauMotDePasse);
            ps.setString(2, login);
            ps.setString(3, nom);
            ps.setString(4, prenom);

            int resultat = ps.executeUpdate();

            return resultat > 0;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }
}
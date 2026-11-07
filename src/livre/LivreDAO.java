package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LivreDAO {

    public void ajouterLivre(
            String titre,
            String auteur,
            String isbn,
            String categorie,
            String editeur,
            String datePublication,
            int nombreExemplaires) {

        String sql = "INSERT INTO livre "
                + "(titre, auteur, isbn, categorie, editeur, date_publication, "
                + "nombre_exemplaires, nombre_disponible) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setString(1, titre);
            ps.setString(2, auteur);
            ps.setString(3, isbn);
            ps.setString(4, categorie);
            ps.setString(5, editeur);
            ps.setString(6, datePublication);
            ps.setInt(7, nombreExemplaires);
            ps.setInt(8, nombreExemplaires);

            ps.executeUpdate();
            System.out.println("Livre ajouté avec succès !");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifierLivre(
            int idLivre,
            String titre,
            String auteur,
            String isbn,
            String categorie,
            String editeur,
            String datePublication,
            int nombreExemplaires) {

        String sql = "UPDATE livre SET "
                + "titre = ?, auteur = ?, isbn = ?, "
                + "categorie = ?, editeur = ?, date_publication = ?, "
                + "nombre_exemplaires = ? "
                + "WHERE id_livre = ?";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setString(1, titre);
            ps.setString(2, auteur);
            ps.setString(3, isbn);
            ps.setString(4, categorie);
            ps.setString(5, editeur);
            ps.setString(6, datePublication);
            ps.setInt(7, nombreExemplaires);
            ps.setInt(8, idLivre);

            ps.executeUpdate();
            System.out.println("Livre modifié avec succès !");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimerLivre(int idLivre) {
        String sql = "DELETE FROM livre WHERE id_livre = ?";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setInt(1, idLivre);
            ps.executeUpdate();
            System.out.println("Livre supprimé avec succès !");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet rechercherLivres(String recherche) {
        String sql = "SELECT * FROM livre "
                + "WHERE titre LIKE ? "
                + "OR auteur LIKE ? "
                + "OR categorie LIKE ?";

        try {
            Connection connexion = Connexion.getConnection();
            PreparedStatement ps = connexion.prepareStatement(sql);

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

    public ResultSet afficherLivres() {
        String sql = "SELECT * FROM livre ORDER BY titre";

        try {
            Connection connexion = Connexion.getConnection();
            PreparedStatement ps = connexion.prepareStatement(sql);

            return ps.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int compterLivres() {
        String sql = "SELECT COUNT(*) FROM livre";

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

    public int compterLivresDisponibles() {
        String sql =
                "SELECT COUNT(*) FROM livre WHERE nombre_disponible > 0";

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
    public int compterExemplairesDisponibles() {
        String sql =
                "SELECT COALESCE(SUM(nombre_disponible), 0) FROM livre";

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

    //suggestion
//    public int compterLivresDisponibles() {
//
//        String sql =
//                "SELECT COUNT(*) FROM livre WHERE nombre_disponible > 0";
//
//        try (Connection connexion = Connexion.getConnection()) {
//
//            if (connexion == null) {
//                return 0;
//            }
//
//            try (PreparedStatement ps = connexion.prepareStatement(sql);
//                 ResultSet rs = ps.executeQuery()) {
//
//                if (rs.next()) {
//                    return rs.getInt(1);
//                }
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return 0;
//    }
}
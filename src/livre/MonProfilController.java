package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MonProfilController {

    @FXML
    private Label lblInitiales;

    @FXML
    private Label lblNomComplet;

    @FXML
    private Label lblLogin;

    @FXML
    private Label lblAdresse;

    @FXML
    private Label lblTelephone;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblDateInscription;

    private int idUtilisateur;

    public void setIdUtilisateur(int idUtilisateur) {

        this.idUtilisateur = idUtilisateur;

        afficherProfil();
    }

    private void afficherProfil() {

        String sql =
                "SELECT u.nom, u.prenom, u.login, "
              + "a.adresse, a.telephone, a.email, "
              + "a.date_inscription "
              + "FROM utilisateur u "
              + "INNER JOIN adherent a "
              + "ON u.id_utilisateur = a.id_utilisateur "
              + "WHERE u.id_utilisateur = ?";

        try (Connection connexion = Connexion.getConnection()) {

            if (connexion == null) {
                lblNomComplet.setText("Base de données indisponible");
                return;
            }

            try (PreparedStatement ps =
                         connexion.prepareStatement(sql)) {

                ps.setInt(1, idUtilisateur);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        String nom = valeurOu(rs.getString("nom"), "");
                        String prenom = valeurOu(rs.getString("prenom"), "");

                        lblNomComplet.setText((prenom + " " + nom).trim());
                        lblInitiales.setText(initiales(prenom, nom));

                        lblLogin.setText(
                                "Identifiant : " + rs.getString("login"));

                        lblAdresse.setText(
                                valeurOu(rs.getString("adresse"),
                                        "Non renseignée"));

                        lblTelephone.setText(
                                valeurOu(rs.getString("telephone"),
                                        "Non renseigné"));

                        lblEmail.setText(
                                valeurOu(rs.getString("email"),
                                        "Non renseigné"));

                        lblDateInscription.setText(
                                formaterDate(
                                        rs.getString("date_inscription")));

                    } else {

                        lblNomComplet.setText("Profil introuvable");
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            lblNomComplet.setText("Erreur de chargement");
        }
    }

    // Renvoie la valeur, ou une valeur par défaut si elle est vide
    private String valeurOu(String valeur, String parDefaut) {

        if (valeur == null || valeur.isBlank()) {
            return parDefaut;
        }

        return valeur;
    }

    // Première lettre du prénom + première lettre du nom
    private String initiales(String prenom, String nom) {

        String resultat = "";

        if (!prenom.isBlank()) {
            resultat += prenom.substring(0, 1);
        }

        if (!nom.isBlank()) {
            resultat += nom.substring(0, 1);
        }

        return resultat.isEmpty() ? "?" : resultat.toUpperCase();
    }

    // Exemple : 2026-09-13 devient 13 septembre 2026
    private String formaterDate(String brut) {

        if (brut == null || brut.isBlank()) {
            return "Non renseignée";
        }

        try {

            return LocalDate.parse(brut.substring(0, 10))
                    .format(DateTimeFormatter.ofPattern(
                            "d MMMM yyyy", Locale.FRENCH));

        } catch (Exception e) {

            return brut;
        }
    }

    @FXML
    private void retour() {

        Stage stage =
                (Stage) lblNomComplet.getScene().getWindow();

        stage.close();
    }
}
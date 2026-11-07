package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MonProfilController1 {

    @FXML
    private Label lblNom;

    @FXML
    private Label lblPrenom;

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

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps =
                     connexion.prepareStatement(sql)) {

            ps.setInt(1, idUtilisateur);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                lblNom.setText(rs.getString("nom"));
                lblPrenom.setText(rs.getString("prenom"));
                lblLogin.setText(rs.getString("login"));

                lblAdresse.setText(
                        rs.getString("adresse") != null
                        ? rs.getString("adresse")
                        : "Non renseignée"
                );

                lblTelephone.setText(
                        rs.getString("telephone") != null
                        ? rs.getString("telephone")
                        : "Non renseigné"
                );

                lblEmail.setText(
                        rs.getString("email") != null
                        ? rs.getString("email")
                        : "Non renseigné"
                );

                lblDateInscription.setText(
                        rs.getString("date_inscription")
                );

            } else {

                lblNom.setText("Profil introuvable");
            }

        } catch (Exception e) {

            e.printStackTrace();

            lblNom.setText("Erreur de chargement");
        }
    }

    @FXML
    private void retour() {

        Stage stage =
                (Stage) lblNom.getScene().getWindow();

        stage.close();
    }
}
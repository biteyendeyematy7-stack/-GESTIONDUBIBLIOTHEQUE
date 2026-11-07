package livre;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class PremiereConnexionController {

    @FXML
    private Label lblLogin;

    @FXML
    private PasswordField txtNouveauMotDePasse;

    @FXML
    private PasswordField txtConfirmation;

    @FXML
    private Label lblMessage;

    private int idUtilisateur;
    private String login;
    private String destinationApresConnexion =
            LoginController.DESTINATION_TABLEAU_DE_BORD;


    // =========================================================
    // Recevoir les informations de l'adhérent
    // =========================================================

    public void setUtilisateur(
            int idUtilisateur,
            String login) {

        this.idUtilisateur = idUtilisateur;
        this.login = login;

        lblLogin.setText(login);
    }

    public void setDestinationApresConnexion(String destination) {
        this.destinationApresConnexion =
                destination == null
                        ? LoginController.DESTINATION_TABLEAU_DE_BORD
                        : destination;
    }


    // =========================================================
    // Valider le nouveau mot de passe
    // =========================================================

    @FXML
    private void valider() {

        String nouveauMotDePasse =
                txtNouveauMotDePasse.getText();

        String confirmation =
                txtConfirmation.getText();


        // Vérifier que les champs sont remplis
        if (nouveauMotDePasse.isEmpty()
                || confirmation.isEmpty()) {

            lblMessage.setText(
                    "Veuillez remplir les deux champs."
            );

            return;
        }


        // Vérifier que les mots de passe correspondent
        if (!nouveauMotDePasse.equals(confirmation)) {

            lblMessage.setText(
                    "Les deux mots de passe ne correspondent pas."
            );

            return;
        }


        // Enregistrer le nouveau mot de passe
        boolean resultat =
                UtilisateurDAO.definirNouveauMotDePasse(
                        idUtilisateur,
                        nouveauMotDePasse
                );


        if (!resultat) {

            lblMessage.setText(
                    "Impossible d'enregistrer le mot de passe."
            );

            return;
        }


        // Mot de passe enregistré
        lblMessage.setText(
                "Mot de passe enregistré avec succès."
        );


        if (LoginController.DESTINATION_PROFIL.equals(
                    destinationApresConnexion)
                || LoginController.DESTINATION_EMPRUNTS.equals(
                    destinationApresConnexion)) {
            ouvrirDestinationAdherent();
        } else {
            ouvrirDashboardAdherent();
        }
    }


    private void ouvrirDestinationAdherent() {
        boolean ouvrirProfil =
                LoginController.DESTINATION_PROFIL.equals(
                        destinationApresConnexion);

        String fichierFXML = ouvrirProfil
                ? "monProfil.fxml"
                : "mesEmprunts.fxml";
        String titre = ouvrirProfil ? "Mon profil" : "Mes emprunts";
        double largeur = ouvrirProfil ? 780 : 1000;
        double hauteur = ouvrirProfil ? 600 : 650;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fichierFXML)
            );

            javafx.scene.Parent racine = loader.load();

            if (ouvrirProfil) {
                MonProfilController controller = loader.getController();
                controller.setIdUtilisateur(idUtilisateur);
            } else {
                MesEmpruntsController controller = loader.getController();
                controller.setIdUtilisateur(idUtilisateur);
            }

            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(racine, largeur, hauteur));
            stage.setResizable(true);
            stage.show();

            Stage ancienneFenetre =
                    (Stage) lblLogin.getScene().getWindow();
            ancienneFenetre.close();

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText(
                    "Erreur lors de l'ouverture de votre espace."
            );
        }
    }


    // =========================================================
    // Ouvrir le dashboard adhérent
    // =========================================================

    private void ouvrirDashboardAdherent() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "dashboardAdherent.fxml"
                            )
                    );


            Scene scene =
                    new Scene(
                            loader.load(),
                            500,
                            500
                    );


            DashboardAdherentController controller =
                    loader.getController();


            controller.setUtilisateur(
                    idUtilisateur,
                    login,
                    "adherent"
            );


            Stage stage = new Stage();

            stage.setTitle(
                    "Gestion de bibliothèque - adherent"
            );

            stage.setScene(scene);
            stage.show();


            Stage ancienneFenetre =
                    (Stage) lblLogin
                            .getScene()
                            .getWindow();

            ancienneFenetre.close();


        } catch (Exception e) {

            e.printStackTrace();

            lblMessage.setText(
                    "Erreur lors de l'ouverture du tableau de bord."
            );
        }
    }
}
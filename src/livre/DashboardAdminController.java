package livre;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DashboardAdminController {

    @FXML
    private Label lblBienvenue;

    private int idUtilisateur;
    private String login;
    private String role;

    public void setUtilisateur(
            int idUtilisateur,
            String login,
            String role) {

        this.idUtilisateur = idUtilisateur;
        this.login = login;
        this.role = role;

        lblBienvenue.setText(
                "Bienvenue, " + login
        );
    }

    @FXML
    private void ouvrirLivres() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource("livre.fxml")
                    );

            Scene scene =
                    new Scene(loader.load(), 900, 600);

            LivreController controller =
                    loader.getController();

            controller.setRole(role);

            Stage stage = new Stage();

            stage.setTitle("Gestion des livres");
            stage.setScene(scene);
            afficherEcranAvecRetour(stage);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirAdherents() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource("adherent.fxml")
                    );

            Scene scene =
                    new Scene(loader.load(), 1100, 720);

            AdherentController controller =
                    loader.getController();

            //controller.setRole(role);
            controller.setUtilisateur(idUtilisateur, login, role);

            // Récupérer la fenêtre actuelle
            Stage stage =
                    (Stage) lblBienvenue.getScene().getWindow();

            stage.setTitle("Gestion des adhérents");
            stage.setScene(scene);
            stage.setResizable(true);

            stage.setMinWidth(900);
            stage.setMinHeight(400);

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirEmprunts() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource("emprunt.fxml")
                    );

            Scene scene =
                    new Scene(loader.load(), 1000, 600);

            EmpruntController controller =
                    loader.getController();

            controller.setUtilisateur(
                    idUtilisateur,
                    role
            );

            Stage stage = new Stage();

            stage.setTitle("Gestion des emprunts");
            stage.setScene(scene);
            afficherEcranAvecRetour(stage);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirStatistiques() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                        getClass().getResource("statistiques.fxml"));

            Scene scene =
                    new Scene(loader.load(), 500, 500);

            Stage stage = new Stage();

            stage.setTitle("Statistiques");
            stage.setScene(scene);
            stage.setResizable(true);
            afficherEcranAvecRetour(stage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void afficherEcranAvecRetour(Stage fenetre) {
        Stage tableauDeBord =
                (Stage) lblBienvenue.getScene().getWindow();

        fenetre.initOwner(tableauDeBord);
        fenetre.setOnHidden(event -> tableauDeBord.show());

        tableauDeBord.hide();
        fenetre.show();
    }

    @FXML
    private void deconnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Acceuil.fxml")
            );

            Scene scene = new Scene(loader.load(), 1100, 550);
            scene.getStylesheets().add(
                    getClass().getResource("accueil.css").toExternalForm()
            );
            Stage stage = (Stage) lblBienvenue.getScene().getWindow();

            stage.setTitle("Gestion de bibliothèque");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
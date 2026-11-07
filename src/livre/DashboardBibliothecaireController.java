package livre;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardBibliothecaireController {

    @FXML
    private Label lblBienvenue;

    private int idUtilisateur;
    private String role;

    public void setUtilisateur(
            int idUtilisateur,
            String login,
            String role) {

        this.idUtilisateur = idUtilisateur;
        this.role = role;

        lblBienvenue.setText("Bienvenue, " + login);
    }

    @FXML
    private void ouvrirLivres() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("livre.fxml"));

            Scene scene = new Scene(loader.load(), 1000, 500);

            LivreController controller = loader.getController();
            controller.setRole(role);

            Stage stage = new Stage();
            stage.setTitle("Gestion des livres");
            stage.setScene(scene);
            stage.setResizable(true);
            afficherEcranAvecRetour(stage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirEmprunts() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("gestionEmprunts.fxml"));

            Scene scene = new Scene(loader.load(), 1000, 650);

            GestionEmpruntsController controller =
                    loader.getController();

            controller.setUtilisateur(idUtilisateur, role);

            Stage stage = new Stage();
            stage.setTitle("Gestion des emprunts");
            stage.setScene(scene);
            stage.setResizable(true);
            afficherEcranAvecRetour(stage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirRetours() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("gestionRetours.fxml"));

            Scene scene = new Scene(loader.load(), 1000, 650);

            GestionRetoursController controller =
                    loader.getController();

            controller.setUtilisateur(idUtilisateur, role);

            Stage stage = new Stage();
            stage.setTitle("Gestion des retours");
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
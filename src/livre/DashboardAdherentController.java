package livre;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardAdherentController {

    @FXML
    private Label lblBienvenue;

    private String role;
    private int idUtilisateur;

    public void setUtilisateur(
            int idUtilisateur,
            String login,
            String role) {

        this.idUtilisateur = idUtilisateur;
        this.role = role;

        lblBienvenue.setText("Bienvenue, " + login);
    }
    @FXML
    private void consulterLivres() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "catalogueLivres.fxml"
                            )
                    );

            Scene scene =
                    new Scene(
                            loader.load(),
                            1000,
                            650
                    );

            CatalogueLivresController controller = loader.getController();
            controller.setUtilisateur(idUtilisateur, role);

            Stage stage = new Stage();
            stage.setTitle("Catalogue des livres");
            stage.setScene(scene);
            stage.setResizable(true);
            afficherEcranAvecRetour(stage);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @FXML
    private void rechercherLivre() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource("rechercheLivre.fxml")
                    );

            Scene scene =
                    new Scene(
                            loader.load(),
                            1000,
                            650
                    );

            Stage stage = new Stage();

            stage.setTitle("Rechercher un livre");
            stage.setScene(scene);
            stage.setResizable(true);
            afficherEcranAvecRetour(stage);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

  

    private void ouvrirCatalogue() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("livre.fxml"));

            Scene scene = new Scene(loader.load(), 1000, 700);

            LivreController controller = loader.getController();
            controller.setRole(role);

            Stage stage = new Stage();
            stage.setTitle("Catalogue des livres");
            stage.setScene(scene);
            stage.setResizable(true);
            afficherEcranAvecRetour(stage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void mesEmprunts() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "mesEmprunts.fxml"
                            )
                    );

            Scene scene =
                    new Scene(
                            loader.load(),
                            1000,
                            650
                    );

            MesEmpruntsController controller =
                    loader.getController();

            controller.setIdUtilisateur(idUtilisateur);

            Stage stage = new Stage();

            stage.setTitle("Mes emprunts");

            stage.setScene(scene);

            stage.setResizable(true);

            afficherEcranAvecRetour(stage);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @FXML
    private void monProfil() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                        getClass().getResource("monProfil.fxml"));

            Scene scene =
                    new Scene(loader.load(), 500, 500);

            MonProfilController controller =
                    loader.getController();

            controller.setIdUtilisateur(idUtilisateur);

            Stage stage = new Stage();

            stage.setTitle("Mon profil");
            stage.setScene(scene);
            stage.setResizable(true);
            afficherEcranAvecRetour(stage);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @FXML
    private void mesDemandesReservations() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("mesDemandesReservations.fxml")
            );

            Scene scene = new Scene(loader.load(), 1000, 650);

            MesDemandesReservationsController controller =
                    loader.getController();
            controller.setIdUtilisateur(idUtilisateur);

            Stage stage = new Stage();
            stage.setTitle("Mes demandes et réservations");
            stage.setScene(scene);
            stage.setResizable(true);
            afficherEcranAvecRetour(stage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void mesNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("mesNotifications.fxml")
            );

            Scene scene = new Scene(loader.load(), 900, 620);

            MesNotificationsController controller =
                    loader.getController();
            controller.setIdUtilisateur(idUtilisateur);

            Stage stage = new Stage();
            stage.setTitle("Mes notifications");
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
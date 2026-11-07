package livre;

import java.net.URL;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AccueilController {

    @FXML
    private Label lblLivresDisponibles;

    @FXML
    private Label lblAdherents;

    @FXML
    private Label lblExemplairesDisponibles;

    private LivreDAO livreDAO = new LivreDAO();
    private AdherentDAO adherentDAO = new AdherentDAO();

    @FXML
    public void initialize() {
        afficherStatistiques();
    }

    private void afficherStatistiques() {
        int nombreLivres = livreDAO.compterLivresDisponibles();
        int nombreAdherents = adherentDAO.compterAdherents();
        int nombreExemplairesDisponibles =
                livreDAO.compterExemplairesDisponibles();

        lblLivresDisponibles.setText(String.valueOf(nombreLivres));
        lblAdherents.setText(String.valueOf(nombreAdherents));
        lblExemplairesDisponibles.setText(
                String.valueOf(nombreExemplairesDisponibles)
        );
    }

    @FXML
    public void ouvrirConnexion(ActionEvent event) {
        ouvrirConnexion(event, LoginController.DESTINATION_TABLEAU_DE_BORD);
    }

    @FXML
    public void ouvrirEspaceAdherent(ActionEvent event) {
        ouvrirConnexion(event, LoginController.DESTINATION_PROFIL);
    }

    @FXML
    public void ouvrirMesEmprunts(ActionEvent event) {
        ouvrirConnexion(event, LoginController.DESTINATION_EMPRUNTS);
    }

    private void ouvrirConnexion(ActionEvent event, String destination) {
        System.out.println("[AccueilController] ouverture connexion demandée, destination=" + destination);
        try {
            URL fxmlUrl = getClass().getResource("login.fxml");
            System.out.println("[AccueilController] URL login.fxml = " + fxmlUrl);
            if (fxmlUrl == null) {
                throw new IllegalStateException("Fichier FXML introuvable : login.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            System.out.println("[AccueilController] FXMLLoader construit pour login.fxml");

            Parent root = loader.load();
            System.out.println("[AccueilController] loader.load() OK, racine = " + root + " ; classe = " + root.getClass().getName());

            if (loader.getController() == null) {
                throw new IllegalStateException("Le contrôleur associé à login.fxml est introuvable.");
            }

            LoginController controller = loader.getController();
            System.out.println("[AccueilController] Contrôleur LoginController récupéré = " + controller);
            controller.setDestinationApresConnexion(destination);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            System.out.println("[AccueilController] Stage source = " + stage + ", visible=" + stage.isShowing());

            stage.setTitle("Connexion - Gestion de bibliothèque");
            Scene scene = new Scene(root, 1280, 760);
            System.out.println("[AccueilController] Nouvelle scène créée, root=" + scene.getRoot());
            stage.setScene(scene);
            stage.show();
            System.out.println("[AccueilController] stage.show() appelé ; stage visible = " + stage.isShowing() + ", largeur=" + stage.getWidth() + ", hauteur=" + stage.getHeight());

        } catch (Exception e) {
            System.err.println("[AccueilController] ERREUR pendant ouverture de login.fxml");
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d’ouverture");
            alert.setHeaderText("Impossible d’ouvrir la page de connexion");
            alert.setContentText(
                    "Le fichier login.fxml est introuvable, invalide ou son contrôleur n’a pas pu être chargé.\n"
                            + e.getMessage()
            );
            alert.showAndWait();
        }
    }

    @FXML
    public void ouvrirCatalogue(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("livre.fxml")
            );

            Scene scene = new Scene(loader.load(), 750, 500);

            LivreController controller = loader.getController();
            controller.setRole("public");

            Stage tableauDeBord = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            Stage stage = new Stage();
            stage.initOwner(tableauDeBord);
            stage.setOnHidden(e -> tableauDeBord.show());
            stage.setTitle("Catalogue des livres");
            stage.setScene(scene);

            tableauDeBord.hide();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d’ouverture");
            alert.setHeaderText("Impossible d’ouvrir le catalogue");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
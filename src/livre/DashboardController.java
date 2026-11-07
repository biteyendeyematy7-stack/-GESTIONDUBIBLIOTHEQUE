package livre;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class DashboardController {
	   @FXML
	    private Label lblBienvenue;

	    @FXML
	    private Label lblRole;

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

	        lblRole.setText(
	                "Rôle : " + role
	        );
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
	    @FXML
	    private void ouvrirLivres() {

	        try {

	            FXMLLoader loader =
	                    new FXMLLoader(
	                            getClass().getResource("livre.fxml")
	                    );

	            Scene scene =
	                    new Scene(loader.load(), 900, 600);

	            // Récupérer le contrôleur de la fenêtre des livres
	            LivreController controller =
	                    loader.getController();

	            // Transmettre le rôle de l'utilisateur connecté
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

	        // Seuls l'administrateur 
	        // peut gérer les adhérents.
	    	if (!"administrateur".equals(role)) {

	    	    System.out.println(
	    	            "Accès refusé : seuls les administrateurs peuvent gérer les adhérents."
	    	    );

	    	    return;
	    	}
	        try {

	            FXMLLoader loader =
	                    new FXMLLoader(
	                            getClass().getResource("adherent.fxml")
	                    );

	            Scene scene =
	                    new Scene(loader.load(), 1000, 600);
	            AdherentController controller =
	                    loader.getController();

	            controller.setRole(role);

	            Stage stage = new Stage();

	            stage.setTitle("Gestion des adhérents");
	            stage.setScene(scene);
	            afficherEcranAvecRetour(stage);

	        } catch (Exception e) {

	            e.printStackTrace();
	        }
	    }
	    @FXML
	    private void ouvrirEmprunts() {

	        // Seuls l'administrateur et le bibliothécaire
	        // peuvent gérer les emprunts.
	        if (!"administrateur".equals(role)
	                && !"bibliothecaire".equals(role)) {

	            System.out.println(
	                    "Accès refusé : un adhérent ne peut pas gérer les emprunts."
	            );

	            return;
	        }

	        try {

	            FXMLLoader loader =
	                    new FXMLLoader(
	                            getClass().getResource("emprunt.fxml")
	                    );

	            Scene scene =
	                    new Scene(loader.load(), 1000, 600);

	            // Récupérer le contrôleur
	            EmpruntController controller =
	                    loader.getController();

	            // Transmettre le rôle
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
}

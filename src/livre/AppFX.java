package livre;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("[AppFX] Début start(Stage)");

        URL fxmlUrl = getClass().getResource("Acceuil.fxml");
        System.out.println("[AppFX] FXML Acceuil : " + fxmlUrl);
        if (fxmlUrl == null) {
            throw new IllegalStateException("Fichier FXML introuvable : Acceuil.fxml");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        System.out.println("[AppFX] FXMLLoader créé pour Acceuil.fxml");

        Scene scene = new Scene(loader.load(), 1100, 550);
        System.out.println("[AppFX] Scene créée : " + scene + " - root=" + scene.getRoot());

        URL cssUrl = getClass().getResource("accueil.css");
        System.out.println("[AppFX] CSS accueil.css : " + cssUrl);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("[AppFX] CSS ajouté à la scène");
        }

        stage.setTitle("Gestion de bibliothèque");
        stage.setScene(scene);
        stage.setResizable(true);
        System.out.println("[AppFX] Stage prêt à être affiché. Taille scène = " + scene.getWidth() + "x" + scene.getHeight());
        stage.show();
        System.out.println("[AppFX] Stage affiché : visible=" + stage.isShowing() + ", width=" + stage.getWidth() + ", height=" + stage.getHeight());
    }

    public static void main(String[] args) {
        System.out.println("[AppFX] Lancement JavaFX");
        launch(args);
    }
}
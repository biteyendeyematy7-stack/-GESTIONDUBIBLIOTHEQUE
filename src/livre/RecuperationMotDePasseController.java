package livre;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RecuperationMotDePasseController {

    @FXML
    private TextField txtLogin;

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtPrenom;

    @FXML
    private PasswordField txtNouveauMotDePasse;

    @FXML
    private PasswordField txtConfirmation;

    @FXML
    private Label lblMessage;

    @FXML
    private void reinitialiserMotDePasse() {

        String login = txtLogin.getText().trim();
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();

        String nouveauMotDePasse =
                txtNouveauMotDePasse.getText();

        String confirmation =
                txtConfirmation.getText();

        if (login.isEmpty()
                || nom.isEmpty()
                || prenom.isEmpty()
                || nouveauMotDePasse.isEmpty()
                || confirmation.isEmpty()) {

            lblMessage.setText(
                    "Veuillez remplir tous les champs."
            );

            return;
        }

        if (!nouveauMotDePasse.equals(confirmation)) {

            lblMessage.setText(
                    "Les deux mots de passe ne correspondent pas."
            );

            return;
        }

        boolean resultat =
                UtilisateurDAO.reinitialiserMotDePasse(
                        login,
                        nom,
                        prenom,
                        nouveauMotDePasse
                );

        if (!resultat) {

            lblMessage.setText(
                    "Les informations saisies ne correspondent à aucun compte."
            );

            return;
        }

        lblMessage.setText(
                "Mot de passe modifié avec succès."
        );

        retourConnexion();

    }

    @FXML
    private void retourConnexion() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "login.fxml"
                            )
                    );

            Scene scene =
                    new Scene(
                            loader.load(),
                            480,
                            520
                    );

            Stage stage =
                    (Stage) txtLogin
                            .getScene()
                            .getWindow();

            stage.setTitle(
                    "Gestion de bibliothèque"
            );

            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            lblMessage.setText(
                    "Erreur lors du retour à la connexion."
            );
        }
    }
}
package livre;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    public static final String DESTINATION_TABLEAU_DE_BORD = "tableauDeBord";
    public static final String DESTINATION_PROFIL = "profil";
    public static final String DESTINATION_EMPRUNTS = "emprunts";

    private String destinationApresConnexion =
            DESTINATION_TABLEAU_DE_BORD;

    public void setDestinationApresConnexion(String destination) {
        this.destinationApresConnexion =
                destination == null ? DESTINATION_TABLEAU_DE_BORD : destination;
    }

    @FXML
    private TextField txtLogin;

    @FXML
    private PasswordField txtMotDePasse;

    @FXML
    private TextField txtMotDePasseVisible;

    @FXML
    private CheckBox chkAfficherMotDePasse;

    @FXML
    private Label lblMessage;

    @FXML
    public void initialize() {
        System.out.println("[LoginController] initialize() : txtLogin=" + txtLogin
                + ", txtMotDePasse=" + txtMotDePasse
                + ", txtMotDePasseVisible=" + txtMotDePasseVisible
                + ", chkAfficherMotDePasse=" + chkAfficherMotDePasse
                + ", lblMessage=" + lblMessage);

        if (txtLogin != null) {
            System.out.println("[LoginController] txtLogin visible=" + txtLogin.isVisible()
                    + ", managed=" + txtLogin.isManaged()
                    + ", prefWidth=" + txtLogin.getPrefWidth()
                    + ", width=" + txtLogin.getWidth());
        }

        if (txtMotDePasse != null) {
            System.out.println("[LoginController] txtMotDePasse visible=" + txtMotDePasse.isVisible()
                    + ", managed=" + txtMotDePasse.isManaged()
                    + ", prefWidth=" + txtMotDePasse.getPrefWidth()
                    + ", width=" + txtMotDePasse.getWidth());
        }

        if (lblMessage != null) {
            System.out.println("[LoginController] lblMessage visible=" + lblMessage.isVisible()
                    + ", opacity=" + lblMessage.getOpacity()
                    + ", text=" + lblMessage.getText());
        }
    }


    @FXML
    private void toggleAffichageMotDePasse() {
        if (chkAfficherMotDePasse.isSelected()) {
            txtMotDePasseVisible.setText(txtMotDePasse.getText());
            txtMotDePasseVisible.setVisible(true);
            txtMotDePasseVisible.setManaged(true);
            txtMotDePasse.setVisible(false);
            txtMotDePasse.setManaged(false);
        } else {
            txtMotDePasse.setText(txtMotDePasseVisible.getText());
            txtMotDePasse.setVisible(true);
            txtMotDePasse.setManaged(true);
            txtMotDePasseVisible.setVisible(false);
            txtMotDePasseVisible.setManaged(false);
        }
    }

    @FXML
    private void seConnecter() {

        String login = txtLogin.getText().trim();
        String motDePasse = chkAfficherMotDePasse.isSelected()
                ? txtMotDePasseVisible.getText()
                : txtMotDePasse.getText();


        // Vérification du login
        if (login.isEmpty()) {

            lblMessage.setText(
                    "Veuillez saisir votre identifiant."
            );
            lblMessage.setStyle("-fx-text-fill: #9B2C2C; -fx-font-weight: bold;");

            return;
        }


        /*
         * =====================================================
         * CAS 1 : vérifier d'abord si le login existe
         * =====================================================
         */

        Utilisateur utilisateur =
                UtilisateurDAO.rechercherParLogin(login);


        if (utilisateur == null) {

            lblMessage.setText(
                    "Identifiant incorrect !"
            );
            lblMessage.setStyle("-fx-text-fill: #9B2C2C; -fx-font-weight: bold;");

            return;
        }


        /*
         * =====================================================
         * CAS 2 : première connexion d'un adhérent
         * =====================================================
         */

        if ("adherent".equals(utilisateur.getRole())
                && UtilisateurDAO.estPremiereConnexion(
                        utilisateur.getIdUtilisateur())) {

            ouvrirPremiereConnexion(utilisateur);

            return;
        }


        /*
         * =====================================================
         * CAS 3 : connexion normale
         * =====================================================
         */

        if (motDePasse.isEmpty()) {

            lblMessage.setText(
                    "Veuillez saisir votre mot de passe."
            );
            lblMessage.setStyle("-fx-text-fill: #9B2C2C; -fx-font-weight: bold;");

            return;
        }


        utilisateur =
                UtilisateurDAO.connecter(
                        login,
                        motDePasse
                );


        if (utilisateur == null) {

            lblMessage.setText(
                    "Login ou mot de passe incorrect !"
            );
            lblMessage.setStyle("-fx-text-fill: #9B2C2C; -fx-font-weight: bold;");

            return;
        }


        if ("adherent".equals(utilisateur.getRole())
                && (DESTINATION_PROFIL.equals(destinationApresConnexion)
                    || DESTINATION_EMPRUNTS.equals(destinationApresConnexion))) {
            ouvrirDestinationAdherent(utilisateur);
        } else {
            ouvrirDashboard(utilisateur);
        }
    }


    private void ouvrirDestinationAdherent(Utilisateur utilisateur) {
        boolean ouvrirProfil =
                DESTINATION_PROFIL.equals(destinationApresConnexion);

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
                controller.setIdUtilisateur(utilisateur.getIdUtilisateur());
            } else {
                MesEmpruntsController controller = loader.getController();
                controller.setIdUtilisateur(utilisateur.getIdUtilisateur());
            }

            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(racine, largeur, hauteur));
            stage.setResizable(true);
            stage.show();

            Stage ancienneFenetre =
                    (Stage) txtLogin.getScene().getWindow();
            ancienneFenetre.close();

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText(
                    "Erreur lors de l'ouverture de votre espace."
            );
        }
    }


    // =========================================================
    // Ouvrir l'écran de première connexion
    // =========================================================

    private void ouvrirPremiereConnexion(
            Utilisateur utilisateur) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "premiereConnexion.fxml"
                            )
                    );

            Scene scene =
                    new Scene(
                            loader.load(),
                            480,
                            520
                    );


            PremiereConnexionController controller =
                    loader.getController();


            controller.setUtilisateur(
                    utilisateur.getIdUtilisateur(),
                    utilisateur.getLogin()
            );
            controller.setDestinationApresConnexion(
                    destinationApresConnexion
            );


            Stage stage = new Stage();

            stage.setTitle(
                    "Première connexion"
            );

            stage.setScene(scene);
            stage.show();


            Stage ancienneFenetre =
                    (Stage) txtLogin
                            .getScene()
                            .getWindow();

            ancienneFenetre.close();


        } catch (Exception e) {

            e.printStackTrace();

            lblMessage.setText(
                    "Erreur lors de l'ouverture de la première connexion."
            );
        }
    }


    // =========================================================
    // Ouvrir le dashboard correspondant au rôle
    // =========================================================

    private void ouvrirDashboard(
            Utilisateur utilisateur) {

        try {

            String fichierFXML;


            switch (utilisateur.getRole()) {

                case "administrateur":
                    fichierFXML =
                            "dashboardAdmin.fxml";
                    break;

                case "bibliothecaire":
                    fichierFXML =
                            "dashboardBibliothecaire.fxml";
                    break;

                case "adherent":
                    fichierFXML =
                            "dashboardAdherent.fxml";
                    break;

                default:

                    lblMessage.setText(
                            "Rôle utilisateur inconnu !"
                    );

                    return;
            }


            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    fichierFXML
                            )
                    );


            Scene scene =
                    new Scene(
                            loader.load(),
                            500,
                            500
                    );


            // =================================================
            // Administrateur
            // =================================================

            if ("administrateur".equals(
                    utilisateur.getRole())) {

                DashboardAdminController controller =
                        loader.getController();

                controller.setUtilisateur(
                        utilisateur.getIdUtilisateur(),
                        utilisateur.getLogin(),
                        utilisateur.getRole()
                );
            }


            // =================================================
            // Bibliothécaire
            // =================================================

            else if ("bibliothecaire".equals(
                    utilisateur.getRole())) {

                DashboardBibliothecaireController controller =
                        loader.getController();

                controller.setUtilisateur(
                        utilisateur.getIdUtilisateur(),
                        utilisateur.getLogin(),
                        utilisateur.getRole()
                );
            }


            // =================================================
            // Adhérent
            // =================================================

            else if ("adherent".equals(
                    utilisateur.getRole())) {

                DashboardAdherentController controller =
                        loader.getController();

                controller.setUtilisateur(
                        utilisateur.getIdUtilisateur(),
                        utilisateur.getLogin(),
                        utilisateur.getRole()
                );
            }


            Stage stage = new Stage();

            stage.setTitle(
                    "Gestion de bibliothèque - "
                    + utilisateur.getRole()
            );

            stage.setScene(scene);
            stage.show();


            Stage ancienneFenetre =
                    (Stage) txtLogin
                            .getScene()
                            .getWindow();

            ancienneFenetre.close();


        } catch (Exception e) {

            e.printStackTrace();

            lblMessage.setText(
                    "Erreur lors de l'ouverture de l'interface."
            );
        }
    }
    @FXML
    private void motDePasseOublie() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "recuperationMotDePasse.fxml"
                            )
                    );

            Scene scene =
                    new Scene(
                            loader.load(),
                            480,
                            620
                    );

            Stage stage = new Stage();

            stage.setTitle("Mot de passe oublié");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();

            Stage ancienneFenetre =
                    (Stage) txtLogin
                            .getScene()
                            .getWindow();

            ancienneFenetre.close();

        } catch (Exception e) {

            e.printStackTrace();

            lblMessage.setText(
                    "Erreur lors de l'ouverture de la récupération."
            );
        }
    }
}
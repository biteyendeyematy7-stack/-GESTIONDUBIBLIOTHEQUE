package livre;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
//import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdherentController {

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtPrenom;

    @FXML
    private TextField txtAdresse;

    @FXML
    private TextField txtTelephone;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtDateInscription;

    @FXML
    private TextField txtLogin;

    //@FXML
   // private PasswordField txtMotDePasse;

   // @FXML
   // private PasswordField txtConfirmationMotDePasse;

    @FXML
    private TextField txtRecherche;

    @FXML
    private FlowPane grilleAdherents;

    @FXML
    private Label lblNombreAdherents;

    @FXML
    private Label lblMessage;

    @FXML
    private Button btnAjouter;

    @FXML
    private Button btnModifier;

    @FXML
    private Button btnSupprimer;

    private String role;
    private int idUtilisateur;
    private String login;

    private AdherentDAO adherentDAO = new AdherentDAO();

    private int idAdherentSelectionne = -1;

    // Carte actuellement sélectionnée dans la grille
    private VBox carteSelectionnee;

    @FXML
    public void initialize() {

        afficherAdherents();

        passerEnModeCreation();
    }

    // =========================================================
    // AFFICHAGE DES ADHERENTS SOUS FORME DE CARTES
    // =========================================================

    private ObservableList<ObservableList<String>> lireAdherents(ResultSet rs)
            throws SQLException {

        ObservableList<ObservableList<String>> donnees =
                FXCollections.observableArrayList();

        while (rs.next()) {

            ObservableList<String> ligne =
                    FXCollections.observableArrayList();

            ligne.add(rs.getString("id_adherent"));
            ligne.add(rs.getString("nom"));
            ligne.add(rs.getString("prenom"));
            ligne.add(rs.getString("telephone"));
            ligne.add(rs.getString("email"));
            ligne.add(rs.getString("date_inscription"));
            ligne.add(rs.getString("login"));
            ligne.add(colonneOptionnelle(rs, "adresse"));

            donnees.add(ligne);
        }

        return donnees;
    }

    // Lit une colonne qui peut ne pas figurer dans la requête
    private String colonneOptionnelle(ResultSet rs, String colonne) {

        try {
            return rs.getString(colonne);
        } catch (SQLException e) {
            return "";
        }
    }

    private void afficherCartes(
            ObservableList<ObservableList<String>> donnees) {

        grilleAdherents.getChildren().clear();
        carteSelectionnee = null;

        if (donnees.isEmpty()) {

            Label vide = new Label("Aucun adhérent à afficher.");
            vide.getStyleClass().add("card-hint");
            grilleAdherents.getChildren().add(vide);
        }

        for (ObservableList<String> ligne : donnees) {

            VBox carte = CartesAdherents.creer(ligne);

            carte.setOnMouseClicked(event -> selectionner(carte, ligne));

            grilleAdherents.getChildren().add(carte);
        }

        lblNombreAdherents.setText(
                donnees.size()
                + (donnees.size() > 1 ? " adhérents" : " adhérent"));
    }

    // Un clic sur une carte remplit le formulaire avec ses informations
    private void selectionner(VBox carte, ObservableList<String> ligne) {

        if (carteSelectionnee != null) {
            carteSelectionnee.getStyleClass()
                    .remove("adherent-carte-selectionnee");
        }

        carteSelectionnee = carte;
        carte.getStyleClass().add("adherent-carte-selectionnee");

        idAdherentSelectionne = Integer.parseInt(ligne.get(0));

        txtNom.setText(valeur(ligne, 1));
        txtPrenom.setText(valeur(ligne, 2));
        txtTelephone.setText(valeur(ligne, 3));
        txtEmail.setText(valeur(ligne, 4));
        txtDateInscription.setText(valeur(ligne, 5));
        txtLogin.setText(valeur(ligne, 6));
        txtAdresse.setText(valeur(ligne, 7));

       // txtMotDePasse.clear();
       // txtConfirmationMotDePasse.clear();

        passerEnModeEdition();

        afficher("Adhérent « " + valeur(ligne, 2) + " "
                + valeur(ligne, 1) + " » sélectionné.", "info");
    }

    private String valeur(ObservableList<String> ligne, int index) {

        if (ligne.size() <= index || ligne.get(index) == null) {
            return "";
        }

        return ligne.get(index);
    }

    // Mode création : formulaire vide, seul "Créer" est disponible
    private void passerEnModeCreation() {

        idAdherentSelectionne = -1;

        if (carteSelectionnee != null) {
            carteSelectionnee.getStyleClass()
                    .remove("adherent-carte-selectionnee");
            carteSelectionnee = null;
        }

        viderChamps();

        txtDateInscription.setText(LocalDate.now().toString());

        txtLogin.setDisable(false);
       // txtMotDePasse.setDisable(false);
        //txtConfirmationMotDePasse.setDisable(false);

        btnAjouter.setDisable(false);
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }

    // Mode modification : l'identifiant et le mot de passe ne changent pas
    private void passerEnModeEdition() {

        txtLogin.setDisable(true);
      //  txtMotDePasse.setDisable(true);
       // txtConfirmationMotDePasse.setDisable(true);

        btnAjouter.setDisable(true);
        btnModifier.setDisable(false);
        btnSupprimer.setDisable(false);
    }

    // Affiche un message coloré : "ok", "erreur" ou "info"
    private void afficher(String texte, String type) {

        lblMessage.getStyleClass().removeAll(
                "status-ok", "status-erreur", "status-info");
        lblMessage.getStyleClass().add("status-" + type);
        lblMessage.setText(texte);
    }

    // Ferme proprement le résultat, son statement et sa connexion
    private void fermer(ResultSet rs) {

        if (rs == null) {
            return;
        }

        try {

            Connection connexion = rs.getStatement().getConnection();
            rs.close();
            connexion.close();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // AFFICHER LES ADHERENTS
    // =========================================================

    private void afficherAdherents() {

        ResultSet rs = null;

        try {

            rs = adherentDAO.afficherAdherents();

            if (rs != null) {
                afficherCartes(lireAdherents(rs));
            }

        } catch (Exception e) {

            e.printStackTrace();
            afficher("Base de données indisponible.", "erreur");

        } finally {

            fermer(rs);
        }
    }

    // =========================================================
    // RECHERCHER UN ADHERENT
    // =========================================================

    @FXML
    private void rechercherAdherent() {

        ResultSet rs = null;

        try {

            rs = adherentDAO.rechercherAdherents(txtRecherche.getText());

            if (rs != null) {
                afficherCartes(lireAdherents(rs));
            }

        } catch (Exception e) {

            e.printStackTrace();
            afficher("Base de données indisponible.", "erreur");

        } finally {

            fermer(rs);
        }
    }

    @FXML
    private void afficherTousLesAdherents() {

        txtRecherche.clear();

        afficherAdherents();
    }

    // =========================================================
    // NOUVEL ADHERENT
    // =========================================================

    @FXML
    private void nouvelAdherent() {

        passerEnModeCreation();

        afficher("Formulaire vidé : vous pouvez créer un nouvel adhérent.",
                "info");
    }

    // =========================================================
    // AJOUTER UN ADHERENT
    // =========================================================

//    @FXML
//    private void ajouterAdherent() {
//
//        if (!"administrateur".equals(role)) {
//
//            afficher("Accès refusé : seul l'administrateur peut gérer les adhérents.",
//                    "erreur");
//
//            return;
//        }
//
//        try {
//
//            String nom = txtNom.getText().trim();
//            String prenom = txtPrenom.getText().trim();
//            String adresse = txtAdresse.getText().trim();
//            String telephone = txtTelephone.getText().trim();
//            String email = txtEmail.getText().trim();
//            String dateInscription = txtDateInscription.getText().trim();
//
//            String login = txtLogin.getText().trim();
//            String motDePasse = txtMotDePasse.getText();
//            String confirmation = txtConfirmationMotDePasse.getText();
//
//            // Vérification des champs obligatoires
//            if (nom.isEmpty()
//                    || prenom.isEmpty()
//                    || dateInscription.isEmpty()
//                    || login.isEmpty()
//                    || motDePasse.isEmpty()
//                    || confirmation.isEmpty()) {
//
//                afficher("Veuillez remplir les champs obligatoires.", "erreur");
//
//                return;
//            }
//
//            // Vérification du mot de passe
//            if (!motDePasse.equals(confirmation)) {
//
//                afficher("Les mots de passe ne correspondent pas !", "erreur");
//
//                return;
//            }

@FXML
private void ajouterAdherent() {

    if (!"administrateur".equals(role)) {
        afficher("Accès refusé : seul l'administrateur peut gérer les adhérents.", "erreur");
        return;
    }

    String nom = txtNom.getText().trim();
    String prenom = txtPrenom.getText().trim();
    String adresse = txtAdresse.getText().trim();
    String telephone = txtTelephone.getText().trim();
    String email = txtEmail.getText().trim();
    String dateInscription = txtDateInscription.getText().trim();
    String login = txtLogin.getText().trim();

    if (nom.isEmpty() || prenom.isEmpty()
            || dateInscription.isEmpty() || login.isEmpty()) {
        afficher("Veuillez remplir les champs obligatoires.", "erreur");
        return;
    }

    try {
        adherentDAO.ajouterAdherent(
                nom, prenom, adresse, telephone,
                email, dateInscription, login
        );

        afficherAdherents();
        passerEnModeCreation();
        afficher("Adhérent créé avec succès !", "ok");

    } catch (Exception e) {
        e.printStackTrace();
        afficher("Erreur lors de la création de l'adhérent.", "erreur");
    }
}

    // =========================================================
    // MODIFIER UN ADHERENT
    // =========================================================

    @FXML
    private void modifierAdherent() {

        if (!"administrateur".equals(role)) {

            afficher("Accès refusé : seul l'administrateur peut gérer les adhérents.",
                    "erreur");

            return;
        }

        if (idAdherentSelectionne == -1) {

            afficher("Cliquez d'abord sur un adhérent.", "erreur");

            return;
        }

        String nom = txtNom.getText();
        String prenom = txtPrenom.getText();
        String adresse = txtAdresse.getText();
        String telephone = txtTelephone.getText();
        String email = txtEmail.getText();
        String dateInscription = txtDateInscription.getText();

        if (nom.isEmpty() || prenom.isEmpty()) {

            afficher("Le nom et le prénom sont obligatoires.", "erreur");

            return;
        }

        adherentDAO.modifierAdherent(
                idAdherentSelectionne,
                nom,
                prenom,
                adresse,
                telephone,
                email,
                dateInscription
        );

        afficherAdherents();

        passerEnModeCreation();

        afficher("Adhérent modifié avec succès !", "ok");
    }

    // =========================================================
    // SUPPRIMER UN ADHERENT
    // =========================================================

    @FXML
    private void supprimerAdherent() {

        if (!"administrateur".equals(role)) {

            afficher("Accès refusé : seul l'administrateur peut gérer les adhérents.",
                    "erreur");

            return;
        }

        if (idAdherentSelectionne == -1) {

            afficher("Cliquez d'abord sur un adhérent.", "erreur");

            return;
        }

        // Boîte de confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Suppression de l'adhérent");
        confirmation.setContentText(
                "Êtes-vous sûr de vouloir supprimer cet adhérent ?");

        ButtonType boutonOui = new ButtonType("Oui");
        ButtonType boutonNon = new ButtonType("Non");

        confirmation.getButtonTypes().setAll(boutonOui, boutonNon);

        ButtonType resultat = confirmation.showAndWait().orElse(boutonNon);

        if (resultat == boutonOui) {

            adherentDAO.supprimerAdherent(idAdherentSelectionne);

            afficherAdherents();

            passerEnModeCreation();

            afficher("Adhérent supprimé avec succès !", "ok");

        } else {

            afficher("Suppression annulée.", "info");
        }
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setUtilisateur(int idUtilisateur, String login, String role) {
        this.idUtilisateur = idUtilisateur;
        this.login = login;
        this.role = role;
    }

    @FXML
    private void retournerDashboard() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource("dashboardAdmin.fxml")
                    );

            Scene scene = new Scene(loader.load());

            DashboardAdminController controller = loader.getController();

            controller.setUtilisateur(
                    idUtilisateur,
                    login,
                    role
            );

            Stage stage =
                    (Stage) grilleAdherents.getScene().getWindow();

            stage.setTitle("Gestion de bibliothèque - administrateur");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
            afficher("Erreur lors du retour au tableau de bord.", "erreur");
        }
    }

    // =========================================================
    // VIDER LES CHAMPS
    // =========================================================

    private void viderChamps() {

        txtNom.clear();
        txtPrenom.clear();
        txtAdresse.clear();
        txtTelephone.clear();
        txtEmail.clear();
        txtDateInscription.clear();
        txtLogin.clear();
       // txtMotDePasse.clear();
       // txtConfirmationMotDePasse.clear();
    }
}

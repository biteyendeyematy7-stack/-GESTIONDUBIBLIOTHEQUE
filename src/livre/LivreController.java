package livre;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LivreController {

    @FXML
    private Button btnAjouter;

    @FXML
    private Button btnModifier;

    @FXML
    private Button btnSupprimer;

    @FXML
    private TextField txtTitre;

    @FXML
    private TextField txtAuteur;

    @FXML
    private TextField txtCategorie;

    @FXML
    private TextField txtISBN;

    @FXML
    private TextField txtEditeur;

    @FXML
    private TextField txtDatePublication;

    @FXML
    private TextField txtNombreExemplaires;

    @FXML
    private TextField txtRecherche;

    @FXML
    private FlowPane grilleLivres;

    @FXML
    private ScrollPane panneauFormulaire;

    @FXML
    private Label lblMessage;

    @FXML
    private Label lblNombreLivres;

    private String role;

    private LivreDAO livreDAO = new LivreDAO();
    private int idLivreSelectionne = -1;

    // Carte actuellement sélectionnée dans la grille
    private VBox carteSelectionnee;

    @FXML
    public void initialize() {

        afficherLivres();
    }

    // ------------------------------------------------------------
    // Affichage du catalogue sous forme de cartes
    // ------------------------------------------------------------

    private ObservableList<ObservableList<String>> lireLivres(ResultSet rs)
            throws SQLException {

        ObservableList<ObservableList<String>> donnees =
                FXCollections.observableArrayList();

        while (rs.next()) {

            ObservableList<String> ligne =
                    FXCollections.observableArrayList();

            ligne.add(rs.getString("id_livre"));
            ligne.add(rs.getString("titre"));
            ligne.add(rs.getString("auteur"));
            ligne.add(rs.getString("categorie"));
            ligne.add(rs.getString("nombre_exemplaires"));
            ligne.add(rs.getString("nombre_disponible"));
            ligne.add(rs.getString("isbn"));
            ligne.add(rs.getString("editeur"));
            ligne.add(rs.getString("date_publication"));

            donnees.add(ligne);
        }

        return donnees;
    }

    private void afficherCartes(
            ObservableList<ObservableList<String>> donnees) {

        grilleLivres.getChildren().clear();
        carteSelectionnee = null;

        if (donnees.isEmpty()) {

            Label vide = new Label("Aucun livre à afficher.");
            vide.getStyleClass().add("card-hint");
            grilleLivres.getChildren().add(vide);
        }

        for (ObservableList<String> ligne : donnees) {

            VBox carte = CartesLivres.creer(ligne);

            carte.setOnMouseClicked(event -> selectionner(carte, ligne));

            grilleLivres.getChildren().add(carte);
        }

        lblNombreLivres.setText(
                donnees.size() + (donnees.size() > 1 ? " livres" : " livre"));
    }

    // Un clic sur une carte remplit le formulaire avec ses informations
    private void selectionner(VBox carte, ObservableList<String> ligne) {

        if (carteSelectionnee != null) {
            carteSelectionnee.getStyleClass().remove("livre-carte-selectionnee");
        }

        carteSelectionnee = carte;
        carte.getStyleClass().add("livre-carte-selectionnee");

        idLivreSelectionne = Integer.parseInt(ligne.get(0));

        txtTitre.setText(valeur(ligne, 1));
        txtAuteur.setText(valeur(ligne, 2));
        txtCategorie.setText(valeur(ligne, 3));
        txtNombreExemplaires.setText(valeur(ligne, 4));
        txtISBN.setText(valeur(ligne, 6));
        txtEditeur.setText(valeur(ligne, 7));
        txtDatePublication.setText(valeur(ligne, 8));

        afficher("« " + valeur(ligne, 1) + " » sélectionné.", "info");
    }

    private String valeur(ObservableList<String> ligne, int index) {

        if (ligne.size() <= index || ligne.get(index) == null) {
            return "";
        }

        return ligne.get(index);
    }

    private void deselectionner() {

        if (carteSelectionnee != null) {
            carteSelectionnee.getStyleClass().remove("livre-carte-selectionnee");
            carteSelectionnee = null;
        }

        idLivreSelectionne = -1;
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

    private void afficherLivres() {

        ResultSet rs = null;

        try {

            rs = livreDAO.afficherLivres();

            if (rs != null) {
                afficherCartes(lireLivres(rs));
            }

        } catch (Exception e) {

            e.printStackTrace();
            afficher("Base de données indisponible.", "erreur");

        } finally {

            fermer(rs);
        }
    }

    @FXML
    private void rechercherLivre() {

        ResultSet rs = null;

        try {

            rs = livreDAO.rechercherLivres(txtRecherche.getText());

            if (rs != null) {
                afficherCartes(lireLivres(rs));
            }

        } catch (Exception e) {

            e.printStackTrace();
            afficher("Base de données indisponible.", "erreur");

        } finally {

            fermer(rs);
        }
    }

    @FXML
    private void afficherTousLesLivres() {

        txtRecherche.clear();

        afficherLivres();
    }

    // ------------------------------------------------------------
    // Actions du formulaire
    // ------------------------------------------------------------

    private boolean accesAutorise() {

        return "administrateur".equals(role)
                || "bibliothecaire".equals(role);
    }

    @FXML
    private void nouveauLivre() {

        viderChamps();
        deselectionner();

        afficher("Formulaire vidé : vous pouvez ajouter un nouveau livre.",
                "info");
    }

    @FXML
    private void ajouterLivre() {

        if (!accesAutorise()) {

            afficher("Accès refusé : vous n'avez pas les droits.", "erreur");

            return;
        }

        try {

            String titre = txtTitre.getText();
            String auteur = txtAuteur.getText();
            String categorie = txtCategorie.getText();
            String isbn = txtISBN.getText();
            String editeur = txtEditeur.getText();
            String datePublication = txtDatePublication.getText();

            int nombreExemplaires =
                    Integer.parseInt(txtNombreExemplaires.getText().trim());

            if (titre.isEmpty() || auteur.isEmpty()) {

                afficher("Le titre et l'auteur sont obligatoires.", "erreur");
                return;
            }

            livreDAO.ajouterLivre(
                    titre,
                    auteur,
                    isbn,
                    categorie,
                    editeur,
                    datePublication,
                    nombreExemplaires
            );

            viderChamps();
            deselectionner();

            afficherLivres();

            afficher("Livre ajouté avec succès !", "ok");

        } catch (NumberFormatException e) {

            afficher("Le nombre d'exemplaires doit être un nombre.", "erreur");
        }
    }

    @FXML
    private void modifierLivre() {

        if (!accesAutorise()) {

            afficher("Accès refusé : vous n'avez pas les droits.", "erreur");

            return;
        }

        if (idLivreSelectionne == -1) {

            afficher("Cliquez d'abord sur un livre.", "erreur");
            return;
        }

        try {

            String titre = txtTitre.getText();
            String auteur = txtAuteur.getText();
            String categorie = txtCategorie.getText();
            String isbn = txtISBN.getText();
            String editeur = txtEditeur.getText();
            String datePublication = txtDatePublication.getText();

            int nombreExemplaires =
                    Integer.parseInt(txtNombreExemplaires.getText().trim());

            if (titre.isEmpty() || auteur.isEmpty()) {

                afficher("Le titre et l'auteur sont obligatoires.", "erreur");

                return;
            }

            livreDAO.modifierLivre(
                    idLivreSelectionne,
                    titre,
                    auteur,
                    isbn,
                    categorie,
                    editeur,
                    datePublication,
                    nombreExemplaires
            );

            viderChamps();
            deselectionner();

            afficherLivres();

            afficher("Livre modifié avec succès !", "ok");

        } catch (NumberFormatException e) {

            afficher("Le nombre d'exemplaires doit être un nombre.", "erreur");
        }
    }

    @FXML
    private void supprimerLivre() {

        if (!accesAutorise()) {

            afficher("Accès refusé : vous n'avez pas les droits.", "erreur");

            return;
        }

        if (idLivreSelectionne == -1) {

            afficher("Cliquez d'abord sur un livre.", "erreur");

            return;
        }

        // Boîte de confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Suppression du livre");
        confirmation.setContentText(
                "Êtes-vous sûr de vouloir supprimer ce livre ?");

        ButtonType boutonOui = new ButtonType("Oui");
        ButtonType boutonNon = new ButtonType("Non");

        confirmation.getButtonTypes().setAll(boutonOui, boutonNon);

        ButtonType resultat = confirmation.showAndWait().orElse(boutonNon);

        if (resultat == boutonOui) {

            livreDAO.supprimerLivre(idLivreSelectionne);

            viderChamps();
            deselectionner();

            afficherLivres();

            afficher("Livre supprimé avec succès !", "ok");

        } else {

            afficher("Suppression annulée.", "info");
        }
    }

    private void viderChamps() {

        txtTitre.clear();
        txtAuteur.clear();
        txtCategorie.clear();
        txtISBN.clear();
        txtEditeur.clear();
        txtDatePublication.clear();
        txtNombreExemplaires.clear();
    }

    public void setRole(String role) {

        this.role = role;

        boolean autorise = accesAutorise();

        btnAjouter.setDisable(!autorise);
        btnModifier.setDisable(!autorise);
        btnSupprimer.setDisable(!autorise);

        txtTitre.setDisable(!autorise);
        txtAuteur.setDisable(!autorise);
        txtCategorie.setDisable(!autorise);
        txtISBN.setDisable(!autorise);
        txtEditeur.setDisable(!autorise);
        txtDatePublication.setDisable(!autorise);
        txtNombreExemplaires.setDisable(!autorise);

        // Le formulaire n'est visible que pour l'administrateur et le bibliothécaire
        panneauFormulaire.setVisible(autorise);
        panneauFormulaire.setManaged(autorise);
    }

    @FXML
    private void retourAuTableauDeBord(ActionEvent event) {

        Stage fenetreActuelle =
                (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

        fenetreActuelle.close();
    }
}

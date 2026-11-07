package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class CatalogueLivresController {

    @FXML
    private FlowPane grilleLivres;

    @FXML
    private Label lblNombreLivres;

    @FXML
    private Label lblMessage;

    @FXML
    private TextField txtRecherche;

    // Tous les livres lus dans la base
    private final ObservableList<ObservableList<String>> toutesLesLignes =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        // La recherche filtre les cartes au fur et à mesure de la saisie
        txtRecherche.textProperty().addListener(
                (obs, ancien, nouveau) -> afficherCartes());

        chargerLivres();
    }

    private void chargerLivres() {

        String sql =
                "SELECT id_livre, titre, auteur, categorie, "
              + "nombre_exemplaires, nombre_disponible, "
              + "isbn, editeur, date_publication "
              + "FROM livre "
              + "ORDER BY titre";

        try (Connection connexion = Connexion.getConnection()) {

            if (connexion == null) {

                lblMessage.setText("Base de données indisponible.");

                return;
            }

            try (PreparedStatement ps = connexion.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                toutesLesLignes.clear();

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

                    toutesLesLignes.add(ligne);
                }
            }

            afficherCartes();

        } catch (SQLException e) {

            e.printStackTrace();

            lblMessage.setText("Erreur lors du chargement des livres.");
        }
    }

    // Affiche les livres qui correspondent à la recherche
    private void afficherCartes() {

        String texte = txtRecherche.getText() == null
                ? "" : txtRecherche.getText().trim().toLowerCase();

        grilleLivres.getChildren().clear();

        int nombre = 0;

        for (ObservableList<String> ligne : toutesLesLignes) {

            String contenu = (ligne.get(1) + " "
                    + ligne.get(2) + " "
                    + ligne.get(3)).toLowerCase();

            if (texte.isEmpty() || contenu.contains(texte)) {

                grilleLivres.getChildren().add(CartesLivres.creer(ligne));
                nombre++;
            }
        }

        if (nombre == 0) {

            Label vide = new Label("Aucun livre ne correspond à votre recherche.");
            vide.getStyleClass().add("card-hint");
            grilleLivres.getChildren().add(vide);
        }

        lblNombreLivres.setText(nombre + (nombre > 1 ? " livres" : " livre"));

        lblMessage.setText(texte.isEmpty()
                ? toutesLesLignes.size() + " livre(s) dans le catalogue."
                : nombre + " résultat(s) pour « " + txtRecherche.getText().trim() + " ».");
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
package livre;

import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RechercheLivreController {

    @FXML
    private TextField txtRecherche;

    @FXML
    private TableView<ObservableList<String>> tableLivres;

    @FXML
    private TableColumn<ObservableList<String>, String> colId;

    @FXML
    private TableColumn<ObservableList<String>, String> colTitre;

    @FXML
    private TableColumn<ObservableList<String>, String> colAuteur;

    @FXML
    private TableColumn<ObservableList<String>, String> colCategorie;

    @FXML
    private TableColumn<ObservableList<String>, String> colExemplaires;

    @FXML
    private TableColumn<ObservableList<String>, String> colDisponibles;

    @FXML
    private Label lblMessage;


    @FXML
    public void initialize() {

        colId.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().get(0)
                ));

        colTitre.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().get(1)
                ));

        colAuteur.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().get(2)
                ));

        colCategorie.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().get(3)
                ));

        colExemplaires.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().get(4)
                ));

        colDisponibles.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().get(5)
                ));

        // Au démarrage, le tableau reste vide
        tableLivres.setItems(
                FXCollections.observableArrayList()
        );

        lblMessage.setText(
                "Saisissez un titre, un auteur ou une catégorie."
        );
    }


    @FXML
    private void rechercher() {

        String recherche =
                txtRecherche.getText().trim();

        // Si aucun mot n'est saisi
        if (recherche.isEmpty()) {

            tableLivres.setItems(
                    FXCollections.observableArrayList()
            );

            lblMessage.setText(
                    "Veuillez saisir un mot à rechercher."
            );

            return;
        }


        ObservableList<ObservableList<String>> livres =
                FXCollections.observableArrayList();

        String sql =
                "SELECT id_livre, titre, auteur, categorie, "
              + "nombre_exemplaires, nombre_disponible "
              + "FROM livre "
              + "WHERE titre LIKE ? "
              + "OR auteur LIKE ? "
              + "OR categorie LIKE ? "
              + "ORDER BY titre";


        try (
                var connexion = Connexion.getConnection();
                var ps = connexion.prepareStatement(sql)
        ) {

            String rechercheSQL =
                    "%" + recherche + "%";

            ps.setString(1, rechercheSQL);
            ps.setString(2, rechercheSQL);
            ps.setString(3, rechercheSQL);

            ResultSet rs =
                    ps.executeQuery();


            while (rs.next()) {

                ObservableList<String> ligne =
                        FXCollections.observableArrayList();

                ligne.add(
                        rs.getString("id_livre")
                );

                ligne.add(
                        rs.getString("titre")
                );

                ligne.add(
                        rs.getString("auteur")
                );

                ligne.add(
                        rs.getString("categorie")
                );

                ligne.add(
                        rs.getString("nombre_exemplaires")
                );

                ligne.add(
                        rs.getString("nombre_disponible")
                );

                livres.add(ligne);
            }


            tableLivres.setItems(livres);


            if (livres.isEmpty()) {

                lblMessage.setText(
                        "Aucun livre trouvé pour : "
                        + recherche
                );

            } else {

                lblMessage.setText(
                        livres.size()
                        + " livre(s) trouvé(s) pour : "
                        + recherche
                );
            }


        } catch (SQLException e) {

            e.printStackTrace();

            lblMessage.setText(
                    "Erreur lors de la recherche."
            );
        }
    }


    @FXML
    private void effacerRecherche() {

        txtRecherche.clear();

        tableLivres.setItems(
                FXCollections.observableArrayList()
        );

        lblMessage.setText(
                "Saisissez un titre, un auteur ou une catégorie."
        );
    }


    @FXML
    private void retourAuTableauDeBord(
            ActionEvent event) {

        Stage fenetreActuelle =
                (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

        fenetreActuelle.close();
    }
}
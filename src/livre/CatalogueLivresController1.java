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
import javafx.stage.Stage;

public class CatalogueLivresController1 {

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

        afficherLivres();
    }


    private void afficherLivres() {

        ObservableList<ObservableList<String>> livres =
                FXCollections.observableArrayList();

        String sql =
                "SELECT id_livre, titre, auteur, categorie, "
              + "nombre_exemplaires, nombre_disponible "
              + "FROM livre "
              + "ORDER BY titre";

        try (
                var connexion = Connexion.getConnection();
                var ps = connexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

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

            lblMessage.setText(
                    livres.size()
                    + " livre(s) dans le catalogue."
            );

        } catch (SQLException e) {

            e.printStackTrace();

            lblMessage.setText(
                    "Erreur lors du chargement des livres."
            );
        }
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
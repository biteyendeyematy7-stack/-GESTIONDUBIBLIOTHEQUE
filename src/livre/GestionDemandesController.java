package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class GestionDemandesController {

    @FXML
    private TableView<ObservableList<String>> tableDemandes;

    @FXML
    private Label lblMessage;

    private int idUtilisateur;
    private String role;

    private final ObservableList<ObservableList<String>> demandes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        TableColumn<ObservableList<String>, String> colId = new TableColumn<>("N°");
        TableColumn<ObservableList<String>, String> colLivre = new TableColumn<>("Livre");
        TableColumn<ObservableList<String>, String> colAdherent = new TableColumn<>("Adhérent");
        TableColumn<ObservableList<String>, String> colDate = new TableColumn<>("Date demande");
        TableColumn<ObservableList<String>, String> colStatut = new TableColumn<>("Statut");
        TableColumn<ObservableList<String>, Void> colActions = new TableColumn<>("Actions");

        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colLivre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        colAdherent.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(3)));
        colStatut.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(4)));

        colActions.setCellFactory(column -> new TableCell<ObservableList<String>, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                ObservableList<String> ligne = getTableView().getItems().get(getIndex());
                int idDemande = Integer.parseInt(ligne.get(0));
                String statut = ligne.get(4);

                if ("en_attente".equalsIgnoreCase(statut)) {
                    Button btnAccepter = new Button("Accepter");
                    btnAccepter.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white;");
                    btnAccepter.setOnAction(e -> accepterDemande(idDemande));

                    Button btnRefuser = new Button("Refuser");
                    btnRefuser.setStyle("-fx-background-color: #C62828; -fx-text-fill: white;");
                    btnRefuser.setOnAction(e -> refuserDemande(idDemande, "Refusé par le bibliothécaire."));

                    HBox box = new HBox(8, btnAccepter, btnRefuser);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                    return;
                }

                if ("acceptee_a_retirer".equalsIgnoreCase(statut)) {
                    Button btnRemise = new Button("Remise");
                    btnRemise.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white;");
                    btnRemise.setOnAction(e -> confirmerRemiseDemande(idDemande));

                    HBox box = new HBox(8, btnRemise);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                    return;
                }

                setGraphic(null);
            }
        });

        tableDemandes.getColumns().setAll(colId, colLivre, colAdherent, colDate, colStatut, colActions);
        tableDemandes.setItems(demandes);

        chargerDemandes();
    }

    public void setUtilisateur(int idUtilisateur, String role) {
        this.idUtilisateur = idUtilisateur;
        this.role = role == null ? "" : role;
        if (tableDemandes != null) {
            chargerDemandes();
        }
    }

    @FXML
    private void retourAuTableauDeBord() {
        Stage stage = (Stage) tableDemandes.getScene().getWindow();
        stage.close();
    }

    private void chargerDemandes() {
        demandes.clear();

        String sql =
                "SELECT d.id_demande, l.titre, CONCAT(a.nom, ' ', a.prenom) AS adherent, "
              + "DATE_FORMAT(d.date_demande, '%d/%m/%Y %H:%i'), d.statut "
              + "FROM demande_emprunt d "
              + "JOIN livre l ON l.id_livre = d.id_livre "
              + "JOIN adherent a ON a.id_adherent = d.id_adherent "
              + "WHERE d.statut IN ('en_attente', 'acceptee_a_retirer') "
              + "ORDER BY d.date_demande DESC";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ObservableList<String> ligne = FXCollections.observableArrayList();
                ligne.add(String.valueOf(rs.getInt("id_demande")));
                ligne.add(rs.getString("titre"));
                ligne.add(rs.getString("adherent"));
                ligne.add(rs.getString(4));
                ligne.add(rs.getString("statut"));
                demandes.add(ligne);
            }

            lblMessage.setText("" + demandes.size() + " demande(s) en attente.");
            lblMessage.setStyle("-fx-text-fill: #1F3A5F;");

        } catch (SQLException e) {
            e.printStackTrace();
            lblMessage.setText("Erreur lors du chargement des demandes.");
            lblMessage.setStyle("-fx-text-fill: #9B2C2C; -fx-font-weight: bold;");
        }
    }

    private void accepterDemande(int idDemande) {
        if (idDemande <= 0) {
            afficherMessage("Demande invalide.", "erreur");
            return;
        }

        DemandeEmpruntController controller = new DemandeEmpruntController();
        String message = controller.accepterDemande(idDemande, idUtilisateur);
        afficherMessage(message, message.contains("Impossible") ? "erreur" : "ok");
        if (!message.contains("Impossible")) {
            chargerDemandes();
        }
    }

    private void refuserDemande(int idDemande, String motif) {
        if (idDemande <= 0) {
            afficherMessage("Demande invalide.", "erreur");
            return;
        }

        DemandeEmpruntController controller = new DemandeEmpruntController();
        String message = controller.refuserDemande(idDemande, idUtilisateur, motif);
        afficherMessage(message, message.contains("Impossible") ? "erreur" : "ok");
        if (!message.contains("Impossible")) {
            chargerDemandes();
        }
    }

    private void confirmerRemiseDemande(int idDemande) {
        if (idDemande <= 0) {
            afficherMessage("Demande invalide.", "erreur");
            return;
        }

        DemandeEmpruntController controller = new DemandeEmpruntController();
        String message = controller.confirmerRemiseDemande(idDemande, idUtilisateur);
        afficherMessage(message, message.contains("Impossible") ? "erreur" : "ok");
        if (!message.contains("Impossible")) {
            chargerDemandes();
        }
    }

    private void afficherMessage(String texte, String type) {
        lblMessage.setText(texte);
        if ("ok".equals(type)) {
            lblMessage.setStyle("-fx-text-fill: #1B5E20; -fx-font-weight: bold;");
        } else if ("erreur".equals(type)) {
            lblMessage.setStyle("-fx-text-fill: #9B2C2C; -fx-font-weight: bold;");
        } else {
            lblMessage.setStyle("-fx-text-fill: #1F3A5F;");
        }
    }
}

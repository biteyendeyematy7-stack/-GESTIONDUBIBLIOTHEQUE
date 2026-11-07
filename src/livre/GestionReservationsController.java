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

public class GestionReservationsController {

    @FXML
    private TableView<ObservableList<String>> tableReservations;

    @FXML
    private Label lblMessage;

    private int idUtilisateur;
    private String role;

    private final ObservableList<ObservableList<String>> reservations = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        TableColumn<ObservableList<String>, String> colId = new TableColumn<>("N°");
        TableColumn<ObservableList<String>, String> colLivre = new TableColumn<>("Livre");
        TableColumn<ObservableList<String>, String> colAdherent = new TableColumn<>("Adhérent");
        TableColumn<ObservableList<String>, String> colDate = new TableColumn<>("Date réservation");
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
                int idReservation = Integer.parseInt(ligne.get(0));

                Button btnNotifier = new Button("Notifier");
                btnNotifier.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white;");
                btnNotifier.setOnAction(e -> notifierReservation(idReservation));

                Button btnValider = new Button("Remise");
                btnValider.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white;");
                btnValider.setOnAction(e -> validerRemiseReservation(idReservation));

                HBox box = new HBox(8, btnNotifier, btnValider);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        tableReservations.getColumns().setAll(colId, colLivre, colAdherent, colDate, colStatut, colActions);
        tableReservations.setItems(reservations);

        chargerReservations();
    }

    public void setUtilisateur(int idUtilisateur, String role) {
        this.idUtilisateur = idUtilisateur;
        this.role = role == null ? "" : role;
        if (tableReservations != null) {
            chargerReservations();
        }
    }

    @FXML
    private void retourAuTableauDeBord() {
        Stage stage = (Stage) tableReservations.getScene().getWindow();
        stage.close();
    }

    private void chargerReservations() {
        reservations.clear();

        String sql =
                "SELECT r.id_reservation, l.titre, CONCAT(a.nom, ' ', a.prenom) AS adherent, "
              + "DATE_FORMAT(r.date_reservation, '%d/%m/%Y %H:%i'), r.statut "
              + "FROM reservation r "
              + "JOIN livre l ON l.id_livre = r.id_livre "
              + "JOIN adherent a ON a.id_adherent = r.id_adherent "
              + "WHERE r.statut IN ('en_attente', 'disponible_a_retirer') "
              + "ORDER BY r.date_reservation DESC";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ObservableList<String> ligne = FXCollections.observableArrayList();
                ligne.add(String.valueOf(rs.getInt("id_reservation")));
                ligne.add(rs.getString("titre"));
                ligne.add(rs.getString("adherent"));
                ligne.add(rs.getString(4));
                ligne.add(rs.getString("statut"));
                reservations.add(ligne);
            }

            lblMessage.setText("" + reservations.size() + " réservation(s) enregistrée(s).");
            lblMessage.setStyle("-fx-text-fill: #1F3A5F;");

        } catch (SQLException e) {
            e.printStackTrace();
            lblMessage.setText("Erreur lors du chargement des réservations.");
            lblMessage.setStyle("-fx-text-fill: #9B2C2C; -fx-font-weight: bold;");
        }
    }

    private void notifierReservation(int idReservation) {
        if (idReservation <= 0) {
            afficherMessage("Réservation invalide.", "erreur");
            return;
        }

        String sql = "SELECT id_livre FROM reservation WHERE id_reservation = ?";
        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idReservation);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                afficherMessage("Réservation introuvable.", "erreur");
                return;
            }

            int idLivre = rs.getInt("id_livre");
            ReservationController controller = new ReservationController();
            String message = controller.notifierLivreDisponible(idLivre);
            afficherMessage(message, message.contains("Aucune") ? "erreur" : "ok");
            if (!message.contains("Aucune")) {
                chargerReservations();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            afficherMessage("Erreur lors de la notification.", "erreur");
        }
    }

    private void validerRemiseReservation(int idReservation) {
        if (idReservation <= 0) {
            afficherMessage("Réservation invalide.", "erreur");
            return;
        }

        ReservationController controller = new ReservationController();
        String message = controller.confirmerRetraitReservation(idReservation, idUtilisateur);
        afficherMessage(message, message.contains("Impossible") ? "erreur" : "ok");
        if (!message.contains("Impossible")) {
            chargerReservations();
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

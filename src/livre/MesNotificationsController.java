package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MesNotificationsController {

    @FXML private VBox conteneurNotifications;
    @FXML private Label lblMessage;

    private int idUtilisateur;
    private int idAdherentConnecte = -1;

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
        this.idAdherentConnecte = new AdherentDAO().getIdAdherentParUtilisateur(idUtilisateur);
        chargerNotifications();
    }

    private void chargerNotifications() {
        conteneurNotifications.getChildren().clear();

        if (idAdherentConnecte <= 0) {
            lblMessage.setText("Aucun adhérent associé à cet utilisateur.");
            return;
        }

        String sql =
                "SELECT type_notification, message, date_envoi, lu "
              + "FROM notification "
              + "WHERE id_adherent = ? "
              + "ORDER BY date_envoi DESC";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idAdherentConnecte);
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    String type = rs.getString("type_notification");
                    String message = rs.getString("message");
                    String date = rs.getString("date_envoi");
                    int lu = rs.getInt("lu");
                    count++;
                    conteneurNotifications.getChildren().add(creerCarte(type, message, date, lu));
                }

                if (count == 0) {
                    lblMessage.setText("Aucune notification pour le moment.");
                    conteneurNotifications.getChildren().add(new Label("Aucune notification reçue."));
                } else {
                    lblMessage.setText(count + (count > 1 ? " notifications" : " notification") + " reçue(s).");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblMessage.setText("Erreur lors du chargement des notifications.");
        }
    }

    private HBox creerCarte(String type, String message, String dateEnvoi, int lu) {
        HBox carte = new HBox(16);
        carte.setAlignment(Pos.CENTER_LEFT);
        carte.setStyle("-fx-padding: 14; -fx-background-color: #F7F7F7; -fx-border-color: #D9D9D9; -fx-border-radius: 10; -fx-background-radius: 10;");

        VBox info = new VBox(6);
        HBox.setHgrow(info, Priority.ALWAYS);

        String libelle = switch (type) {
            case "rappel_retour" -> "Rappel de retour";
            case "retard_retour" -> "Retard de retour";
            case "demande_emprunt" -> "Demande d’emprunt";
            case "reservation" -> "Réservation";
            default -> "Notification";
        };

        Label lblType = new Label(libelle);
        lblType.setStyle("-fx-font-weight: bold; -fx-text-fill: #1F3A5F;");

        Label lblMessage = new Label(message);
        lblMessage.setWrapText(true);

        Label lblDate = new Label(dateEnvoi == null ? "Date inconnue" : formaterDate(dateEnvoi));
        lblDate.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        Label lblLu = new Label(lu == 1 ? "Lue" : "Nouvelle");
        lblLu.setStyle(lu == 1
                ? "-fx-background-color: #E5E7EB; -fx-text-fill: #374151; -fx-padding: 4 8; -fx-background-radius: 999;"
                : "-fx-background-color: #DFF2E5; -fx-text-fill: #1B5E20; -fx-padding: 4 8; -fx-background-radius: 999;");

        info.getChildren().addAll(lblType, lblMessage, lblDate);
        carte.getChildren().addAll(info, lblLu);
        return carte;
    }

    private String formaterDate(String valeur) {
        try {
            LocalDate date = LocalDate.parse(valeur.substring(0, 10));
            return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH));
        } catch (Exception e) {
            return valeur;
        }
    }

    @FXML
    private void retourAuTableauDeBord(ActionEvent event) {
        Stage fenetreActuelle = (Stage) ((Node) event.getSource()).getScene().getWindow();
        fenetreActuelle.close();
    }
}
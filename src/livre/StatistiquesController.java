package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class StatistiquesController {

    @FXML
    private Label lblLivres;

    @FXML
    private Label lblDisponibles;

    @FXML
    private Label lblAdherents;

    @FXML
    private Label lblEmprunts;

    @FXML
    private Label lblRetards;

    @FXML
    private Label lblRetours;

    @FXML
    private PieChart chartEmprunts;

    @FXML
    public void initialize() {

        afficherStatistiques();
    }

    private void afficherStatistiques() {

        try (Connection connexion = Connexion.getConnection()) {

            // Nombre total de titres
            int livres = compter(connexion,
                    "SELECT COUNT(*) FROM livre");

            // Nombre d'exemplaires disponibles
            int disponibles = compter(connexion,
                    "SELECT COALESCE(SUM(nombre_disponible), 0) "
                  + "FROM livre");

            // Nombre d'adhérents
            int adherents = compter(connexion,
                    "SELECT COUNT(*) FROM adherent");

            // Emprunts en cours (retard compris)
            int enCours = compter(connexion,
                    "SELECT COUNT(*) FROM emprunt "
                  + "WHERE date_retour_reelle IS NULL");

            // Emprunts en retard
            int retards = compter(connexion,
                    "SELECT COUNT(*) FROM emprunt "
                  + "WHERE date_retour_reelle IS NULL "
                  + "AND date_retour_prevue < CURDATE()");

            // Emprunts retournés
            int retours = compter(connexion,
                    "SELECT COUNT(*) FROM emprunt "
                  + "WHERE date_retour_reelle IS NOT NULL");

            lblLivres.setText(String.valueOf(livres));
            lblDisponibles.setText(String.valueOf(disponibles));
            lblAdherents.setText(String.valueOf(adherents));
            lblEmprunts.setText(String.valueOf(enCours));
            lblRetards.setText(String.valueOf(retards));
            lblRetours.setText(String.valueOf(retours));

            afficherGraphique(enCours, retards, retours);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Exécute une requête qui renvoie un seul nombre
    private int compter(Connection connexion, String sql)
            throws SQLException {

        try (PreparedStatement ps = connexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Graphique : en cours (hors retard), en retard, retournés.
    // L'ordre est important : il détermine les couleurs dans le CSS.
    private void afficherGraphique(int enCours, int retards, int retours) {

        int enCoursSansRetard = Math.max(0, enCours - retards);

        chartEmprunts.setData(FXCollections.observableArrayList(
                new PieChart.Data("En cours (" + enCoursSansRetard + ")",
                        enCoursSansRetard),
                new PieChart.Data("En retard (" + retards + ")",
                        retards),
                new PieChart.Data("Retournés (" + retours + ")",
                        retours)
        ));
    }

    @FXML
    private void fermer() {

        Stage stage =
                (Stage) lblLivres.getScene().getWindow();

        stage.close();
    }
}
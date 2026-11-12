package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MesDemandesReservationsController {

    @FXML private VBox conteneurItems;
    @FXML private Label lblMessage;
    @FXML private Button btnTous;
    @FXML private Button btnDemandes;
    @FXML private Button btnReservations;
    @FXML private Button btnHistorique;

    private int idUtilisateur;
    private int idAdherentConnecte = -1;
    private String filtreActif = "tous";

    private final List<PendingItem> items = new ArrayList<>();

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
        this.idAdherentConnecte = new AdherentDAO().getIdAdherentParUtilisateur(idUtilisateur);
        chargerElements();
    }

    @FXML
    public void initialize() {
        mettreAJourFiltres();
    }

    private void chargerElements() {
        items.clear();

        if (idAdherentConnecte <= 0) {
            lblMessage.setText("Aucun adhérent associé à cet utilisateur.");
            afficherItems();
            return;
        }

        String sqlDemandes =
                "SELECT d.id_demande AS id_item, 'Demande d’emprunt' AS type, l.titre, l.auteur, l.categorie, d.date_demande AS date_item, "
              + "CASE d.statut "
              + "WHEN 'en_attente' THEN 'En attente' "
              + "WHEN 'acceptee_a_retirer' THEN 'Acceptée — à retirer' "
              + "WHEN 'refusee' THEN 'Refusée' "
              + "WHEN 'expiree' THEN 'Expirée' "
              + "ELSE d.statut END AS statut "
              + "FROM demande_emprunt d "
              + "JOIN livre l ON l.id_livre = d.id_livre "
              + "WHERE d.id_adherent = ? "
              + "ORDER BY d.date_demande DESC";

        String sqlReservations =
                "SELECT r.id_reservation AS id_item, 'Réservation' AS type, l.titre, l.auteur, l.categorie, r.date_reservation AS date_item, "
              + "CASE r.statut "
              + "WHEN 'en_attente' THEN 'En attente' "
              + "WHEN 'disponible_a_retirer' THEN 'Disponible — à retirer' "
              + "WHEN 'expiree' THEN 'Expirée' "
              + "ELSE r.statut END AS statut "
              + "FROM reservation r "
              + "JOIN livre l ON l.id_livre = r.id_livre "
              + "WHERE r.id_adherent = ? "
              + "ORDER BY r.date_reservation DESC";

        try (Connection connexion = Connexion.getConnection()) {
            if (connexion == null) {
                lblMessage.setText("Connexion à la base de données indisponible.");
                afficherItems();
                return;
            }

            try (PreparedStatement ps = connexion.prepareStatement(sqlDemandes)) {
                ps.setInt(1, idAdherentConnecte);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(new PendingItem(
                                rs.getString("type"),
                                rs.getString("titre"),
                                rs.getString("auteur"),
                                rs.getString("categorie"),
                                rs.getString("date_item"),
                                rs.getString("statut"),
                                rs.getInt("id_item")
                        ));
                    }
                }
            }

            try (PreparedStatement ps = connexion.prepareStatement(sqlReservations)) {
                ps.setInt(1, idAdherentConnecte);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(new PendingItem(
                                rs.getString("type"),
                                rs.getString("titre"),
                                rs.getString("auteur"),
                                rs.getString("categorie"),
                                rs.getString("date_item"),
                                rs.getString("statut"),
                                rs.getInt("id_item")
                        ));
                    }
                }
            }

            mettreAJourFiltres();
            afficherItems();

        } catch (SQLException e) {
            e.printStackTrace();
            lblMessage.setText("Erreur lors du chargement de vos demandes et réservations.");
            afficherItems();
        }
    }

    private void mettreAJourFiltres() {
        btnTous.setText("Tous (" + items.size() + ")");
        btnDemandes.setText("Demandes (" + compterType("Demande d’emprunt") + ")");
        btnReservations.setText("Réservations (" + compterType("Réservation") + ")");
        btnHistorique.setText("Historique (" + compterHistorique() + ")");

        definirStyleFiltre(btnTous, "tous".equals(filtreActif));
        definirStyleFiltre(btnDemandes, "demandes".equals(filtreActif));
        definirStyleFiltre(btnReservations, "reservations".equals(filtreActif));
        definirStyleFiltre(btnHistorique, "historique".equals(filtreActif));
    }

    private void definirStyleFiltre(Button bouton, boolean actif) {
        bouton.getStyleClass().remove("loan-filter-active");
        if (actif) {
            bouton.getStyleClass().add("loan-filter-active");
        }
    }

    private long compterType(String type) {
        return items.stream().filter(item -> type.equals(item.type)).count();
    }

    private long compterHistorique() {
        return items.stream().filter(item -> !"En attente".equals(item.statut)
                && !"Acceptée — à retirer".equals(item.statut)
                && !"Disponible — à retirer".equals(item.statut)).count();
    }

    @FXML
    private void filtrerTous() { appliquerFiltre("tous"); }

    @FXML
    private void filtrerDemandes() { appliquerFiltre("demandes"); }

    @FXML
    private void filtrerReservations() { appliquerFiltre("reservations"); }

    @FXML
    private void filtrerHistorique() { appliquerFiltre("historique"); }

    private void appliquerFiltre(String filtre) {
        filtreActif = filtre;
        mettreAJourFiltres();
        afficherItems();
    }

    private void afficherItems() {
        conteneurItems.getChildren().clear();

        List<PendingItem> resultats = items.stream().filter(this::correspondAuFiltre).toList();

        if (resultats.isEmpty()) {
            Label vide = new Label(messageVide());
            vide.getStyleClass().add("loan-empty-state");
            conteneurItems.getChildren().add(vide);
            return;
        }

        for (PendingItem item : resultats) {
            conteneurItems.getChildren().add(creerCarte(item));
        }
    }

    private boolean correspondAuFiltre(PendingItem item) {
        if ("tous".equals(filtreActif)) return true;
        if ("demandes".equals(filtreActif)) return "Demande d’emprunt".equals(item.type);
        if ("reservations".equals(filtreActif)) return "Réservation".equals(item.type);
        if ("historique".equals(filtreActif)) return !"En attente".equals(item.statut)
                && !"Acceptée — à retirer".equals(item.statut)
                && !"Disponible — à retirer".equals(item.statut);
        return true;
    }

    private String messageVide() {
        if (items.isEmpty()) {
            return "Aucune demande ni réservation pour le moment.";
        }
        if ("demandes".equals(filtreActif)) return "Aucune demande d’emprunt.";
        if ("reservations".equals(filtreActif)) return "Aucune réservation.";
        if ("historique".equals(filtreActif)) return "Aucun historique à afficher.";
        return "Aucun élément à afficher.";
    }

    private boolean estDemandeAnnulable(String statut) {
        return "En attente".equals(statut)
                || "Acceptée — à retirer".equals(statut);
    }

    private boolean estReservationAnnulable(String statut) {
        return "En attente".equals(statut)
                || "Disponible — à retirer".equals(statut);
    }

    private HBox creerCarte(PendingItem item) {
        HBox carte = new HBox(20);
        carte.setAlignment(Pos.CENTER_LEFT);
        carte.getStyleClass().addAll("loan-card", "loan-card-current");

        VBox couverture = CartesLivres.creerCouverture(item.titre, item.auteur, item.categorie);

        VBox details = new VBox(12);
        details.setFillWidth(true);
        HBox.setHgrow(details, Priority.ALWAYS);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label type = new Label(item.type);
        type.getStyleClass().add("loan-title");

        Label statut = new Label(item.statut);
        statut.getStyleClass().addAll("loan-status", "loan-status-current");

        header.getChildren().addAll(type, statut);

        Label date = new Label(item.date == null ? "Date non renseignée" : "Date : " + formaterDate(item.date));
        date.getStyleClass().add("loan-id");

        details.getChildren().addAll(header, date);

        if ("Demande d’emprunt".equals(item.type) && estDemandeAnnulable(item.statut)) {
            Button btnAnnuler = new Button("Annuler");
            btnAnnuler.setOnAction(this::annulerDemande);
            btnAnnuler.setUserData(item.idItem);
            btnAnnuler.getStyleClass().addAll("annuler-button", "danger-button");
            details.getChildren().add(btnAnnuler);
        }

        if ("Réservation".equals(item.type) && estReservationAnnulable(item.statut)) {
            Button btnAnnuler = new Button("Annuler");
            btnAnnuler.setOnAction(this::annulerReservation);
            btnAnnuler.setUserData(item.idItem);
            btnAnnuler.getStyleClass().addAll("annuler-button", "danger-button");
            details.getChildren().add(btnAnnuler);
        }

        carte.getChildren().addAll(couverture, details);
        return carte;
    }

    private String formaterDate(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return "—";
        }

        String dateISO = valeur.length() >= 10 ? valeur.substring(0, 10) : valeur;
        try {
            return LocalDate.parse(dateISO).format(FORMAT_DATE);
        } catch (DateTimeParseException e) {
            return valeur;
        }
    }

    @FXML
    private void retourAuTableauDeBord(ActionEvent event) {
        Stage fenetreActuelle =
                (Stage) ((Node) event.getSource()).getScene().getWindow();
        fenetreActuelle.close();
    }

    @FXML
    private void annulerDemande(ActionEvent event) {
        Object source = event.getSource();
        if (!(source instanceof Button bouton)) {
            return;
        }

        String dataValue = bouton.getUserData() == null ? null : bouton.getUserData().toString();
        if (dataValue == null || dataValue.isBlank()) {
            lblMessage.setText("Aucune demande sélectionnée pour l’annulation.");
            return;
        }

        try {
            int idDemande = Integer.parseInt(dataValue);
            String message = new DemandeEmpruntController().annulerDemande(idDemande, idAdherentConnecte);
            lblMessage.setText(message);
            chargerElements();
        } catch (NumberFormatException e) {
            lblMessage.setText("Demande invalide.");
        }
    }

    @FXML
    private void annulerReservation(ActionEvent event) {
        Object source = event.getSource();
        if (!(source instanceof Button bouton)) {
            return;
        }

        Object data = bouton.getUserData();
        if (data == null) {
            lblMessage.setText("Aucune réservation sélectionnée pour l’annulation.");
            return;
        }

        try {
            int idReservation = Integer.parseInt(data.toString());
            String message = new ReservationController().annulerReservation(idReservation, idAdherentConnecte);
            lblMessage.setText(message);
            chargerElements();
        } catch (NumberFormatException e) {
            lblMessage.setText("Réservation invalide.");
        }
    }

    private static final class PendingItem {
        private final String type;
        private final String titre;
        private final String auteur;
        private final String categorie;
        private final String date;
        private final String statut;
        private final int idItem;

        private PendingItem(String type, String titre, String auteur, String categorie, String date, String statut, int idItem) {
            this.type = type;
            this.titre = titre;
            this.auteur = auteur;
            this.categorie = categorie;
            this.date = date;
            this.statut = statut;
            this.idItem = idItem;
        }
    }
}

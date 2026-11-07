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
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MesEmpruntsController {

    @FXML private VBox conteneurEmprunts;
    @FXML private Label lblMessage;
    @FXML private Button btnTous;
    @FXML private Button btnEnCours;
    @FXML private Button btnEnRetard;
    @FXML private Button btnRetournes;

    private final List<Emprunt> emprunts = new ArrayList<>();
    private int idUtilisateur;
    private String filtreActif = "tous";

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
        chargerEmprunts();
    }

    @FXML
    public void initialize() {
        mettreAJourFiltres();
    }

    private void chargerEmprunts() {
        emprunts.clear();

        String sql =
                "SELECT e.id_emprunt, "
              + "l.titre, "
              + "l.auteur, "
              + "l.categorie, "
              + "e.date_emprunt, "
              + "e.date_retour_prevue, "
              + "e.date_retour_reelle, "
              + "CASE "
              + "WHEN e.date_retour_reelle IS NOT NULL "
              + "THEN 'retourne' "
              + "WHEN e.date_retour_prevue < CURDATE() "
              + "THEN 'en retard' "
              + "ELSE 'en cours' "
              + "END AS statut "
              + "FROM emprunt e "
              + "INNER JOIN adherent a "
              + "ON e.id_adherent = a.id_adherent "
              + "INNER JOIN livre l "
              + "ON e.id_livre = l.id_livre "
              + "WHERE a.id_utilisateur = ? "
              + "ORDER BY e.id_emprunt DESC";

        try (Connection connexion = Connexion.getConnection()) {
            if (connexion == null) {
                lblMessage.setText("Connexion à la base de données indisponible.");
                afficherEmprunts();
                return;
            }

            try (PreparedStatement ps = connexion.prepareStatement(sql)) {
                ps.setInt(1, idUtilisateur);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        emprunts.add(new Emprunt(
                                rs.getString("id_emprunt"),
                                rs.getString("titre"),
                                rs.getString("auteur"),
                                rs.getString("categorie"),
                                rs.getString("date_emprunt"),
                                rs.getString("date_retour_prevue"),
                                rs.getString("date_retour_reelle"),
                                rs.getString("statut")
                        ));
                    }
                }
            }

            mettreAJourFiltres();
            afficherEmprunts();

        } catch (SQLException e) {
            e.printStackTrace();
            lblMessage.setText("Erreur lors du chargement de vos emprunts.");
            mettreAJourFiltres();
            afficherEmprunts();
        }
    }

    private void mettreAJourFiltres() {
        btnTous.setText("Tous (" + emprunts.size() + ")");
        btnEnCours.setText("En cours (" + compter("en cours") + ")");
        btnEnRetard.setText("En retard (" + compter("en retard") + ")");
        btnRetournes.setText("Retournés (" + compter("retourne") + ")");

        definirStyleFiltre(btnTous, "tous".equals(filtreActif));
        definirStyleFiltre(btnEnCours, "en cours".equals(filtreActif));
        definirStyleFiltre(btnEnRetard, "en retard".equals(filtreActif));
        definirStyleFiltre(btnRetournes, "retourne".equals(filtreActif));
    }

    private void definirStyleFiltre(Button bouton, boolean actif) {
        bouton.getStyleClass().remove("loan-filter-active");
        if (actif) {
            bouton.getStyleClass().add("loan-filter-active");
        }
    }

    private long compter(String statut) {
        return emprunts.stream()
                .filter(emprunt -> statut.equals(emprunt.statut))
                .count();
    }

    @FXML
    private void filtrerTous() {
        appliquerFiltre("tous");
    }

    @FXML
    private void filtrerEnCours() {
        appliquerFiltre("en cours");
    }

    @FXML
    private void filtrerEnRetard() {
        appliquerFiltre("en retard");
    }

    @FXML
    private void filtrerRetournes() {
        appliquerFiltre("retourne");
    }

    private void appliquerFiltre(String filtre) {
        filtreActif = filtre;
        mettreAJourFiltres();
        afficherEmprunts();
    }

    private void afficherEmprunts() {
        conteneurEmprunts.getChildren().clear();

        List<Emprunt> resultat = emprunts.stream()
                .filter(this::correspondAuFiltre)
                .toList();

        lblMessage.setText(emprunts.size()
                + (emprunts.size() > 1
                    ? " emprunts enregistrés."
                    : " emprunt enregistré."));

        if (resultat.isEmpty()) {
            Label vide = new Label(messageVide());
            vide.getStyleClass().add("loan-empty-state");
            conteneurEmprunts.getChildren().add(vide);
            return;
        }

        for (Emprunt emprunt : resultat) {
            conteneurEmprunts.getChildren().add(creerCarte(emprunt));
        }
    }

    private boolean correspondAuFiltre(Emprunt emprunt) {
        return "tous".equals(filtreActif)
                || filtreActif.equals(emprunt.statut);
    }

    private String messageVide() {
        if (emprunts.isEmpty()) {
            return "Vous n'avez aucun emprunt pour le moment.";
        }
        if ("en cours".equals(filtreActif)) {
            return "Aucun emprunt en cours.";
        }
        if ("en retard".equals(filtreActif)) {
            return "Aucun emprunt en retard.";
        }
        if ("retourne".equals(filtreActif)) {
            return "Aucun emprunt retourné.";
        }
        return "Aucun emprunt à afficher.";
    }

    private HBox creerCarte(Emprunt emprunt) {
        HBox carte = new HBox(22);
        carte.setAlignment(Pos.CENTER_LEFT);
        carte.getStyleClass().addAll("loan-card", classeCarte(emprunt.statut));

        VBox couverture = CartesLivres.creerCouverture(
                emprunt.titre,
                emprunt.auteur,
                emprunt.categorie
        );

        VBox details = new VBox(16);
        details.setFillWidth(true);
        HBox.setHgrow(details, Priority.ALWAYS);

        HBox titreEtStatut = new HBox(12);
        titreEtStatut.setAlignment(Pos.CENTER_LEFT);

        VBox titreBloc = new VBox(5);
        HBox.setHgrow(titreBloc, Priority.ALWAYS);

        Label titre = new Label("Suivi de l'emprunt");
        titre.getStyleClass().add("loan-title");

        Label numero = new Label("Emprunt n°" + emprunt.id);
        numero.getStyleClass().add("loan-id");
        titreBloc.getChildren().addAll(titre, numero);

        Label statut = new Label(libelleStatut(emprunt.statut));
        statut.getStyleClass().addAll("loan-status", classeStatut(emprunt.statut));

        titreEtStatut.getChildren().addAll(titreBloc, statut);

        Separator separateur = new Separator();

        HBox dates = new HBox(20);
        dates.setAlignment(Pos.CENTER_LEFT);
        dates.getChildren().addAll(
                creerBlocDate("EMPRUNTÉ LE", formaterDate(emprunt.dateEmprunt)),
                creerSeparateurVertical(),
                creerBlocDate("RETOUR PRÉVU", formaterDate(emprunt.dateRetourPrevue)),
                creerSeparateurVertical(),
                creerBlocDate(
                        "RETOUR EFFECTIF",
                        emprunt.dateRetourReelle == null
                                ? "Pas encore rendu"
                                : formaterDate(emprunt.dateRetourReelle)
                )
        );

        details.getChildren().addAll(titreEtStatut, separateur, dates);
        carte.getChildren().addAll(couverture, details);
        return carte;
    }

    private VBox creerBlocDate(String titre, String valeur) {
        VBox bloc = new VBox(6);
        bloc.setMinWidth(0);
        HBox.setHgrow(bloc, Priority.ALWAYS);

        Label libelle = new Label(titre);
        libelle.getStyleClass().add("loan-date-caption");

        Label date = new Label(valeur);
        date.setWrapText(true);
        date.getStyleClass().add("loan-date-value");

        bloc.getChildren().addAll(libelle, date);
        return bloc;
    }

    private Region creerSeparateurVertical() {
        Separator separateur = new Separator();
        separateur.setOrientation(javafx.geometry.Orientation.VERTICAL);
        separateur.setMinHeight(42);
        return separateur;
    }

    private String formaterDate(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return "—";
        }

        String dateISO = valeur.length() >= 10
                ? valeur.substring(0, 10)
                : valeur;

        try {
            return LocalDate.parse(dateISO).format(FORMAT_DATE);
        } catch (DateTimeParseException e) {
            return valeur;
        }
    }

    private String valeurOuDefaut(String valeur, String defaut) {
        return valeur == null || valeur.isBlank() ? defaut : valeur;
    }

    private String libelleStatut(String statut) {
        if ("en retard".equals(statut)) {
            return "EN RETARD";
        }
        if ("retourne".equals(statut)) {
            return "RETOURNÉ";
        }
        return "EN COURS";
    }

    private String classeCarte(String statut) {
        if ("en retard".equals(statut)) {
            return "loan-card-overdue";
        }
        if ("retourne".equals(statut)) {
            return "loan-card-returned";
        }
        return "loan-card-current";
    }

    private String classeStatut(String statut) {
        if ("en retard".equals(statut)) {
            return "loan-status-overdue";
        }
        if ("retourne".equals(statut)) {
            return "loan-status-returned";
        }
        return "loan-status-current";
    }

    @FXML
    private void retourAuTableauDeBord(ActionEvent event) {
        Stage fenetreActuelle =
                (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

        fenetreActuelle.close();
    }

    private static final class Emprunt {
        private final String id;
        private final String titre;
        private final String auteur;
        private final String categorie;
        private final String dateEmprunt;
        private final String dateRetourPrevue;
        private final String dateRetourReelle;
        private final String statut;

        private Emprunt(
                String id,
                String titre,
                String auteur,
                String categorie,
                String dateEmprunt,
                String dateRetourPrevue,
                String dateRetourReelle,
                String statut) {
            this.id = id;
            this.titre = titre;
            this.auteur = auteur;
            this.categorie = categorie;
            this.dateEmprunt = dateEmprunt;
            this.dateRetourPrevue = dateRetourPrevue;
            this.dateRetourReelle = dateRetourReelle;
            this.statut = statut == null ? "en cours" : statut.toLowerCase(Locale.ROOT);
        }
    }
}

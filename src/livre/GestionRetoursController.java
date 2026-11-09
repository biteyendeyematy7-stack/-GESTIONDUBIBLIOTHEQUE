package livre;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class GestionRetoursController {

    private int idRetourSelectionne = -1;

    @FXML
    private ComboBox<String> cmbEmpruntRetour;

    @FXML
    private DatePicker dateRetourReelle;

    @FXML
    private TableView<ObservableList<String>> tableRetours;

    @FXML
    private TableColumn<ObservableList<String>, String> colId;

    @FXML
    private TableColumn<ObservableList<String>, String> colLivre;

    @FXML
    private TableColumn<ObservableList<String>, String> colNom;

    @FXML
    private TableColumn<ObservableList<String>, String> colPrenom;

    @FXML
    private TableColumn<ObservableList<String>, String> colRetourPrevu;

    @FXML
    private TableColumn<ObservableList<String>, String> colRetourReel;

    @FXML
    private TableColumn<ObservableList<String>, String> colEcart;

    @FXML
    private Label lblMessage;

    @FXML
    private Label lblARendre;

    @FXML
    private TextField txtRecherche;

    private EmpruntDAO empruntDAO = new EmpruntDAO();

    private int idUtilisateur;
    private String role;
    private Map<String, Integer> retoursIds;
    private Map<String, Integer> empruntsIds;

    // Toutes les lignes, puis la version filtrée affichée dans le tableau
    private final ObservableList<ObservableList<String>> toutesLesLignes =
            FXCollections.observableArrayList();

    private FilteredList<ObservableList<String>> lignesFiltrees;

    @FXML
    public void initialize() {

        lignesFiltrees = new FilteredList<>(toutesLesLignes, ligne -> true);
        tableRetours.setItems(lignesFiltrees);

        colonne(colId, 0);
        colonne(colLivre, 1);
        colonne(colNom, 2);
        colonne(colPrenom, 3);
        colonne(colRetourPrevu, 4);
        colonne(colRetourReel, 5);
        colonne(colEcart, 6);

        // Délai affiché sous forme de puce : à temps (vert) ou en retard (rouge)
        colEcart.setCellFactory(column ->
                new TableCell<ObservableList<String>, String>() {

                    private final Label puce = new Label();

                    @Override
                    protected void updateItem(String ecart, boolean empty) {

                        super.updateItem(ecart, empty);

                        if (empty || ecart == null) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }

                        puce.getStyleClass().setAll("label", "chip");
                        puce.setText(ecart);

                        if (ecart.startsWith("Retard")) {
                            puce.getStyleClass().add("chip-en-retard");
                        } else if (ecart.startsWith("À temps")) {
                            puce.getStyleClass().add("chip-retourne");
                        }

                        setText(null);
                        setGraphic(puce);
                    }
                });

        tableRetours.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, ancienneLigne, nouvelleLigne) -> {

                    if (nouvelleLigne != null) {

                        idRetourSelectionne =
                                Integer.parseInt(nouvelleLigne.get(0));

                        dateRetourReelle.setValue(
                                lireDate(nouvelleLigne.get(5)));

                        afficher("Retour n°" + idRetourSelectionne
                                + " sélectionné.", "info");
                    }
                });

        txtRecherche.textProperty().addListener(
                (obs, ancien, nouveau) -> appliquerRecherche());

        chargerEmpruntsPourRetour();
    }

    public void setUtilisateur(
            int idUtilisateur,
            String role) {

        this.idUtilisateur = idUtilisateur;
        this.role = role;
    }

    // Associe une colonne à une position dans la ligne
    private void colonne(
            TableColumn<ObservableList<String>, String> colonne, int index) {

        colonne.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(index)));
    }

    // Lit une date au format 2026-09-23 (ou 2026-09-23 00:00:00)
    private LocalDate lireDate(String brut) {

        try {
            return LocalDate.parse(brut.trim().substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    // Affiche un message coloré : "ok", "erreur" ou "info"
    private void afficher(String texte, String type) {

        lblMessage.getStyleClass().removeAll(
                "status-ok", "status-erreur", "status-info");
        lblMessage.getStyleClass().add("status-" + type);
        lblMessage.setText(texte);
    }

    private void appliquerRecherche() {

        String texte = txtRecherche.getText() == null
                ? "" : txtRecherche.getText().trim().toLowerCase();

        lignesFiltrees.setPredicate(ligne -> {

            if (texte.isEmpty()) {
                return true;
            }

            String contenu = (ligne.get(1) + " "
                    + ligne.get(2) + " "
                    + ligne.get(3)).toLowerCase();

            return contenu.contains(texte);
        });
    }

    // Écart entre la date prévue et la date réelle de retour
    private String calculerEcart(String prevu, String reel) {

        LocalDate datePrevue = lireDate(prevu);
        LocalDate dateReelle = lireDate(reel);

        if (datePrevue == null || dateReelle == null) {
            return "—";
        }

        long jours = ChronoUnit.DAYS.between(datePrevue, dateReelle);

        if (jours > 0) {
            return "Retard de " + jours + (jours > 1 ? " jours" : " jour");
        }

        return "À temps";
    }

    private void chargerEmpruntsPourRetour() {

        empruntsIds = empruntDAO.getEmpruntsEnCours();

        cmbEmpruntRetour.setValue(null);
        cmbEmpruntRetour.getItems().clear();
        cmbEmpruntRetour.getItems().addAll(empruntsIds.keySet());

        int nombre = empruntsIds.size();

        if (lblARendre != null) {

            if (nombre == 0) {
                lblARendre.setText("Aucun emprunt en attente de retour.");
            } else {
                lblARendre.setText(nombre
                        + (nombre > 1
                                ? " emprunts en attente de retour."
                                : " emprunt en attente de retour."));
            }
        }

        afficherRetours();
    }

    // Remplit l'historique des retours déjà enregistrés
    private void afficherRetours() {

        ObservableList<ObservableList<String>> donnees =
                FXCollections.observableArrayList();

        retoursIds = empruntDAO.getEmpruntsRetournes();

        for (Map.Entry<String, Integer> entry : retoursIds.entrySet()) {

            String[] morceaux = entry.getKey().split(" - ");

            if (morceaux.length >= 5) {

                ObservableList<String> ligne =
                        FXCollections.observableArrayList();

                ligne.add(String.valueOf(entry.getValue()));
                ligne.add(morceaux[0]);
                ligne.add(morceaux[1]);
                ligne.add(morceaux[2]);
                ligne.add(morceaux[3]);
                ligne.add(morceaux[4]);
                ligne.add(calculerEcart(morceaux[3], morceaux[4]));

                donnees.add(ligne);
            }
        }

        // Les retours les plus récents en premier
        donnees.sort((a, b) -> Integer.compare(
                Integer.parseInt(b.get(0)),
                Integer.parseInt(a.get(0))));

        toutesLesLignes.setAll(donnees);
    }

    @FXML
    private void enregistrerRetour() {

        if (cmbEmpruntRetour.getValue() == null
                || dateRetourReelle.getValue() == null) {

            afficher("Veuillez sélectionner un emprunt et une date.",
                    "erreur");

            return;
        }

        int idEmprunt =
                empruntsIds.get(cmbEmpruntRetour.getValue());

        String dateRetour =
                dateRetourReelle.getValue().toString();

        boolean resultat =
                empruntDAO.enregistrerRetour(
                        idEmprunt,
                        dateRetour);

        if (resultat) {

            afficher("Retour enregistré avec succès.", "ok");

            dateRetourReelle.setValue(null);

            chargerEmpruntsPourRetour();

            envoyerNotificationRetour(idEmprunt, dateRetour);

        } else {

            afficher("Impossible d'enregistrer le retour.", "erreur");
        }
    }

    private void envoyerNotificationRetour(int idEmprunt, String dateRetour) {
        if (idEmprunt <= 0) {
            return;
        }

        int idAdherent = empruntDAO.getIdAdherentParEmprunt(idEmprunt);
        if (idAdherent <= 0) {
            return;
        }

        String datePrevue = empruntDAO.getDateRetourPrevueParEmprunt(idEmprunt);
        if (datePrevue == null || datePrevue.isBlank()) {
            return;
        }

        try {
            LocalDate prevu = LocalDate.parse(datePrevue.substring(0, 10));
            LocalDate reel = LocalDate.parse(dateRetour.substring(0, 10));
            long retard = ChronoUnit.DAYS.between(prevu, reel);

            if (retard > 0) {
                String message = "Retard constaté : retour prévu le " + prevu + ", retour réel le " + reel + ". Écart : " + retard + (retard > 1 ? " jours." : " jour.");
                new NotificationDAO().ajouterNotification(idAdherent, "retard_retour", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void envoyerNotificationRetardSiNecessaire(int idEmprunt, String dateRetour) {
        if (idEmprunt <= 0 || dateRetour == null || dateRetour.isBlank()) {
            return;
        }

        int idAdherent = empruntDAO.getIdAdherentParEmprunt(idEmprunt);
        if (idAdherent <= 0) {
            return;
        }

        String datePrevue = empruntDAO.getDateRetourPrevueParEmprunt(idEmprunt);
        if (datePrevue == null || datePrevue.isBlank()) {
            return;
        }

        try {
            LocalDate prevu = LocalDate.parse(datePrevue.substring(0, 10));
            LocalDate reel = LocalDate.parse(dateRetour.substring(0, 10));
            long retard = ChronoUnit.DAYS.between(prevu, reel);

            if (retard > 0) {
                String message = "Retard constaté : retour prévu le "
                        + prevu + ", retour réel le " + reel
                        + ". Écart : " + retard + (retard > 1 ? " jours." : " jour.");
                new NotificationDAO().ajouterNotification(idAdherent, "retard_retour", message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void retourAuTableauDeBord() {

        Stage stage =
                (Stage) tableRetours.getScene().getWindow();

        stage.close();
    }

    @FXML
    private void modifierRetour() {

        if (idRetourSelectionne == -1) {

            afficher("Veuillez sélectionner un retour.", "erreur");

            return;
        }

        if (dateRetourReelle.getValue() == null) {

            afficher("Veuillez sélectionner une date.", "erreur");

            return;
        }

        int idRetourAMettreAJour = idRetourSelectionne;
        String nouvelleDateRetour = dateRetourReelle.getValue().toString();

        boolean resultat = empruntDAO.modifierDateRetour(
                idRetourAMettreAJour,
                nouvelleDateRetour
        );

        if (resultat) {

            afficher("Date de retour modifiée avec succès.", "ok");

            envoyerNotificationRetardSiNecessaire(idRetourAMettreAJour, nouvelleDateRetour);

            idRetourSelectionne = -1;

            dateRetourReelle.setValue(null);

            tableRetours.getSelectionModel().clearSelection();

            afficherRetours();

        } else {

            afficher("Impossible de modifier la date de retour.",
                    "erreur");
        }
    }
}
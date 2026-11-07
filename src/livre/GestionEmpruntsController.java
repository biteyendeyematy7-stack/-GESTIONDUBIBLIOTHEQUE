package livre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
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
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class GestionEmpruntsController {

    @FXML
    private ComboBox<String> cmbLivre;

    @FXML
    private ComboBox<String> cmbAdherent;

    @FXML
    private DatePicker dateEmprunt;

    @FXML
    private DatePicker dateRetourPrevue;

    @FXML
    private TableView<ObservableList<String>> tableEmprunts;

    @FXML
    private TableColumn<ObservableList<String>, String> colId;

    @FXML
    private TableColumn<ObservableList<String>, String> colLivre;

    @FXML
    private TableColumn<ObservableList<String>, String> colNom;

    @FXML
    private TableColumn<ObservableList<String>, String> colPrenom;

    @FXML
    private TableColumn<ObservableList<String>, String> colDateEmprunt;

    @FXML
    private TableColumn<ObservableList<String>, String> colDateRetour;

    @FXML
    private TableColumn<ObservableList<String>, String> colStatut;

    @FXML
    private Label lblMessage;

    @FXML
    private TextField txtRecherche;

    @FXML
    private ToggleGroup groupeFiltre;

    @FXML
    private ToggleButton btnTous;

    @FXML
    private ToggleButton btnEnCours;

    @FXML
    private ToggleButton btnRetard;

    @FXML
    private ToggleButton btnRetourne;

    private EmpruntDAO empruntDAO = new EmpruntDAO();

    private int idUtilisateur;
    private String role;

    private Map<String, Integer> livresIds;
    private Map<String, Integer> adherentsIds;
    private int idEmpruntSelectionne = -1;

    // Toutes les lignes, puis la version filtrée affichée dans le tableau
    private final ObservableList<ObservableList<String>> toutesLesLignes =
            FXCollections.observableArrayList();

    private FilteredList<ObservableList<String>> lignesFiltrees;

    @FXML
    public void initialize() {

        lignesFiltrees = new FilteredList<>(toutesLesLignes, ligne -> true);
        tableEmprunts.setItems(lignesFiltrees);

        colonne(colId, 0);
        colonne(colLivre, 1);
        colonne(colNom, 2);
        colonne(colPrenom, 3);
        colonne(colDateEmprunt, 4);
        colonne(colDateRetour, 5);
        colonne(colStatut, 6);

        // Statut affiché sous forme de puce colorée
        colStatut.setCellFactory(column ->
                new TableCell<ObservableList<String>, String>() {

                    private final Label puce = new Label();

                    @Override
                    protected void updateItem(String statut, boolean empty) {

                        super.updateItem(statut, empty);

                        if (empty || statut == null) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }

                        puce.getStyleClass().setAll("label", "chip");

                        switch (statut.trim().toLowerCase()) {

                            case "en cours":
                                puce.setText("En cours");
                                puce.getStyleClass().add("chip-en-cours");
                                break;

                            case "en retard":
                                puce.setText("En retard");
                                puce.getStyleClass().add("chip-en-retard");
                                break;

                            case "retourne":
                                puce.setText("Retourné");
                                puce.getStyleClass().add("chip-retourne");
                                break;

                            default:
                                puce.setText(statut);
                                break;
                        }

                        setText(null);
                        setGraphic(puce);
                    }
                });

        // Les emprunts en retard sont légèrement teintés
        tableEmprunts.setRowFactory(tv ->
                new TableRow<ObservableList<String>>() {

                    @Override
                    protected void updateItem(
                            ObservableList<String> ligne, boolean empty) {

                        super.updateItem(ligne, empty);

                        getStyleClass().remove("ligne-retard");

                        if (!empty && ligne != null
                                && "en retard".equals(ligne.get(6))) {
                            getStyleClass().add("ligne-retard");
                        }
                    }
                });

        // Quand on sélectionne un emprunt dans le tableau
        tableEmprunts.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, ancienneLigne, nouvelleLigne) -> {

                    if (nouvelleLigne != null) {

                        idEmpruntSelectionne =
                                Integer.parseInt(nouvelleLigne.get(0));

                        dateEmprunt.setValue(lireDate(nouvelleLigne.get(4)));
                        dateRetourPrevue.setValue(lireDate(nouvelleLigne.get(5)));

                        afficher("Emprunt n°" + idEmpruntSelectionne
                                + " sélectionné.", "info");
                    }
                });

        // Recherche et filtres
        txtRecherche.textProperty().addListener(
                (obs, ancien, nouveau) -> appliquerFiltres());

        groupeFiltre.selectedToggleProperty().addListener(
                (obs, ancien, nouveau) -> {

                    // Un filtre doit toujours rester sélectionné
                    if (nouveau == null && ancien != null) {
                        ancien.setSelected(true);
                        return;
                    }

                    appliquerFiltres();
                });

        chargerLivresDisponibles();
        chargerAdherents();
        afficherEmprunts();
    }

    public void setUtilisateur(int idUtilisateur, String role) {

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

    private void appliquerFiltres() {

        String texte = txtRecherche.getText() == null
                ? "" : txtRecherche.getText().trim().toLowerCase();

        String statutVoulu = statutDuFiltre();

        lignesFiltrees.setPredicate(ligne -> {

            if (statutVoulu != null && !statutVoulu.equals(ligne.get(6))) {
                return false;
            }

            if (texte.isEmpty()) {
                return true;
            }

            String contenu = (ligne.get(1) + " "
                    + ligne.get(2) + " "
                    + ligne.get(3)).toLowerCase();

            return contenu.contains(texte);
        });
    }

    // Statut correspondant au bouton de filtre actif (null = tous)
    private String statutDuFiltre() {

        Toggle choix = groupeFiltre.getSelectedToggle();

        if (choix == btnEnCours) {
            return "en cours";
        }

        if (choix == btnRetard) {
            return "en retard";
        }

        if (choix == btnRetourne) {
            return "retourne";
        }

        return null;
    }

    private void chargerLivresDisponibles() {

        livresIds = empruntDAO.getLivresDisponibles();

        cmbLivre.getItems().clear();
        cmbLivre.getItems().addAll(livresIds.keySet());
    }

    private void chargerAdherents() {

        adherentsIds = empruntDAO.getAdherents();

        cmbAdherent.getItems().clear();
        cmbAdherent.getItems().addAll(adherentsIds.keySet());
    }

    private void afficherEmprunts() {

        ObservableList<ObservableList<String>> donnees =
                FXCollections.observableArrayList();

        String sql =
                "SELECT e.id_emprunt, "
              + "l.titre, "
              + "a.nom, "
              + "a.prenom, "
              + "e.date_emprunt, "
              + "e.date_retour_prevue, "
              + "CASE "
              + "WHEN e.date_retour_reelle IS NOT NULL THEN 'retourne' "
              + "WHEN e.date_retour_prevue < CURDATE() THEN 'en retard' "
              + "ELSE 'en cours' "
              + "END AS statut "
              + "FROM emprunt e "
              + "INNER JOIN livre l ON e.id_livre = l.id_livre "
              + "INNER JOIN adherent a ON e.id_adherent = a.id_adherent "
              + "ORDER BY e.id_emprunt DESC";

        int nbCours = 0;
        int nbRetard = 0;
        int nbRetourne = 0;

        try (Connection connexion = Connexion.getConnection()) {

            if (connexion == null) {
                afficher("Base de données indisponible.", "erreur");
                return;
            }

            try (PreparedStatement ps = connexion.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    ObservableList<String> ligne =
                            FXCollections.observableArrayList();

                    ligne.add(String.valueOf(rs.getInt("id_emprunt")));
                    ligne.add(rs.getString("titre"));
                    ligne.add(rs.getString("nom"));
                    ligne.add(rs.getString("prenom"));
                    ligne.add(rs.getString("date_emprunt"));
                    ligne.add(rs.getString("date_retour_prevue"));
                    ligne.add(rs.getString("statut"));

                    switch (rs.getString("statut")) {
                        case "en cours":  nbCours++;    break;
                        case "en retard": nbRetard++;   break;
                        default:          nbRetourne++; break;
                    }

                    donnees.add(ligne);
                }
            }

            toutesLesLignes.setAll(donnees);

            btnTous.setText("Tous (" + donnees.size() + ")");
            btnEnCours.setText("En cours (" + nbCours + ")");
            btnRetard.setText("En retard (" + nbRetard + ")");
            btnRetourne.setText("Retournés (" + nbRetourne + ")");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ajouterEmprunt() {

        if (cmbLivre.getValue() == null
                || cmbAdherent.getValue() == null
                || dateEmprunt.getValue() == null
                || dateRetourPrevue.getValue() == null) {

            afficher("Veuillez remplir tous les champs.", "erreur");

            return;
        }

        int idLivre = livresIds.get(cmbLivre.getValue());

        int idAdherent = adherentsIds.get(cmbAdherent.getValue());

        String date1 = dateEmprunt.getValue().toString();

        String date2 = dateRetourPrevue.getValue().toString();

        boolean resultat =
                empruntDAO.ajouterEmprunt(
                        idLivre,
                        idAdherent,
                        idUtilisateur,
                        date1,
                        date2);

        if (resultat) {

            afficher("Emprunt enregistré avec succès.", "ok");

            afficherEmprunts();

            cmbLivre.setValue(null);
            cmbAdherent.setValue(null);
            dateEmprunt.setValue(null);
            dateRetourPrevue.setValue(null);

            chargerLivresDisponibles();

        } else {

            afficher("Impossible d'enregistrer l'emprunt.", "erreur");
        }
    }

    @FXML
    private void retourAuTableauDeBord() {

        Stage stage =
                (Stage) tableEmprunts.getScene().getWindow();

        stage.close();
    }

    @FXML
    private void modifierEmprunt() {

        if (idEmpruntSelectionne == -1) {

            afficher("Veuillez sélectionner un emprunt.", "erreur");

            return;
        }

        if (dateEmprunt.getValue() == null
                || dateRetourPrevue.getValue() == null) {

            afficher("Veuillez sélectionner les deux dates.", "erreur");

            return;
        }

        if (dateRetourPrevue.getValue()
                .isBefore(dateEmprunt.getValue())) {

            afficher("La date de retour prévue doit être "
                    + "postérieure ou égale à la date d'emprunt.",
                    "erreur");

            return;
        }

        boolean resultat =
                empruntDAO.modifierEmpruntDates(
                        idEmpruntSelectionne,
                        dateEmprunt.getValue().toString(),
                        dateRetourPrevue.getValue().toString()
                );

        if (resultat) {

            afficher("Emprunt modifié avec succès.", "ok");

            afficherEmprunts();

            idEmpruntSelectionne = -1;

            dateEmprunt.setValue(null);
            dateRetourPrevue.setValue(null);

            tableEmprunts.getSelectionModel().clearSelection();

        } else {

            afficher("Impossible de modifier l'emprunt.", "erreur");
        }
    }
}

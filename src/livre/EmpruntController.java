package livre;

import java.util.Map;
import javafx.scene.control.DatePicker;

import javafx.scene.control.ComboBox;
import java.sql.ResultSet;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class EmpruntController {

	@FXML
	private ComboBox<String> cmbLivre;

	@FXML
	private ComboBox<String> cmbAdherent;

	@FXML
	private DatePicker txtDateEmprunt;

	@FXML
	private DatePicker txtDateRetourPrevue;

	@FXML
	private DatePicker txtDateRetourReelle;
   
    @FXML
    private ComboBox<String> cmbEmpruntRetour;

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
    private TableColumn<ObservableList<String>, String> colDateRetourReelle;

    @FXML
    private TableColumn<ObservableList<String>, String> colStatut;

    @FXML
    private Label lblMessage;

    private EmpruntDAO empruntDAO = new EmpruntDAO();
    private int idUtilisateur;
    private String role;
    private Map<String, Integer> livresIds;
    private Map<String, Integer> adherentsIds;
    private Map<String, Integer> empruntsIds;


    @FXML
    public void initialize() {

        // Configuration des colonnes

        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().get(0)));

        colLivre.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().get(1)));

        colNom.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().get(2)));

        colPrenom.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().get(3)));

        colDateEmprunt.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().get(4)));

        colDateRetour.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().get(5)));

        colDateRetourReelle.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().get(6)));

        colStatut.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().get(7)));
        colStatut.setCellFactory(column ->
        new javafx.scene.control.TableCell<ObservableList<String>, String>() {

            @Override
            protected void updateItem(String statut, boolean empty) {

                super.updateItem(statut, empty);

                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(statut);
                setAlignment(javafx.geometry.Pos.CENTER);

                switch (statut.trim().toLowerCase()) {

                    case "en cours":

                        setStyle(
                           // "-fx-background-color: #D9F2E3;" +
                            "-fx-text-fill: #198754;" +
                            "-fx-font-weight: bold;"
                        );
                        break;

                    case "en retard":

                        setStyle(
                           // "-fx-background-color: #FADADA;" +
                            "-fx-text-fill: #C62828;" +
                            "-fx-font-weight: bold;"
                        );
                        break;

                    case "retourne":

                        setStyle(
                            //"-fx-background-color: #EEE6F7;" +
                            "-fx-text-fill: #8E6BBE;" +
                            "-fx-font-weight: bold;"
                        );
                        break;

                    default:

                        setStyle(
                            "-fx-background-color: transparent;" +
                            "-fx-text-fill: black;"
                        );
                        break;
                }
            }
        }
    );
        chargerLivresDisponibles();
        chargerAdherents();
        chargerEmpruntsPourRetour();
        afficherEmprunts();

     
    }


    private void afficherEmprunts() {

        ObservableList<ObservableList<String>> donnees =
                FXCollections.observableArrayList();

        ResultSet rs = empruntDAO.afficherEmprunts();

        if (rs != null) {

            try {

                while (rs.next()) {

                    ObservableList<String> ligne =
                            FXCollections.observableArrayList();

                    ligne.add(rs.getString("id_emprunt"));
                    ligne.add(rs.getString("titre"));
                    ligne.add(rs.getString("nom"));
                    ligne.add(rs.getString("prenom"));
                    ligne.add(rs.getString("date_emprunt"));
                    ligne.add(rs.getString("date_retour_prevue"));
                    ligne.add(rs.getString("date_retour_reelle"));
                    ligne.add(rs.getString("statut"));

                    donnees.add(ligne);
                }

                tableEmprunts.setItems(donnees);

            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
    }


    @FXML
    private void ajouterEmprunt() {

        try {

        	String livreSelectionne = cmbLivre.getValue();
        	String adherentSelectionne = cmbAdherent.getValue();

        	if (livreSelectionne == null || adherentSelectionne == null) {

        	    lblMessage.setText(
        	            "Veuillez choisir un livre et un adhérent."
        	    );

        	    return;
        	}

        	int idLivre = livresIds.get(livreSelectionne);
        	int idAdherent = adherentsIds.get(adherentSelectionne);
          
        	if (txtDateEmprunt.getValue() == null
        	        || txtDateRetourPrevue.getValue() == null) {

        	    lblMessage.setText(
        	            "Veuillez sélectionner les dates."
        	    );

        	    return;
        	}

        	String dateEmprunt =
        	        txtDateEmprunt.getValue().toString();

        	String dateRetourPrevue =
        	        txtDateRetourPrevue.getValue().toString();

            // Vérification des champs
            if (dateEmprunt.isEmpty() || dateRetourPrevue.isEmpty()) {

                lblMessage.setText(
                        "Veuillez remplir les dates."
                );

                return;
            }


            // Enregistrement de l'emprunt
            empruntDAO.ajouterEmprunt(
                    idLivre,
                    idAdherent,
                    idUtilisateur,
                    dateEmprunt,
                    dateRetourPrevue
            );


            // Si aucune erreur n'est survenue
            lblMessage.setText(
                    "Emprunt enregistré avec succès !"
            );



            // Vider les champs
         // Actualiser le tableau
            afficherEmprunts();

            // Vider les champs
            cmbLivre.getSelectionModel().clearSelection();
            cmbAdherent.getSelectionModel().clearSelection();
            txtDateEmprunt.setValue(null);
            txtDateRetourPrevue.setValue(null);
            // Actualiser la liste des livres disponibles
            chargerLivresDisponibles();


        } catch (NumberFormatException e) {

            lblMessage.setText(
                    "Les IDs doivent être des nombres."
            );


        } catch (RuntimeException e) {

            lblMessage.setText(
                    e.getMessage()
            );


        } catch (Exception e) {

            e.printStackTrace();

            lblMessage.setText(
                    "Une erreur est survenue."
            );
        }
    }
    private void chargerEmpruntsPourRetour() {

        empruntsIds = empruntDAO.getEmpruntsEnCours();

        cmbEmpruntRetour.getItems().clear();

        for (String emprunt : empruntsIds.keySet()) {
            cmbEmpruntRetour.getItems().add(emprunt);
        }
    }
    @FXML
    private void enregistrerRetour() {
    	

    	    String empruntSelectionne =
    	            cmbEmpruntRetour.getValue();

    	    if (empruntSelectionne == null) {

    	        lblMessage.setText(
    	                "Veuillez sélectionner un emprunt."
    	        );

    	        return;
    	    }

    	    int idEmprunt =
    	            empruntsIds.get(empruntSelectionne);
    	    if (txtDateRetourReelle.getValue() == null) {

    	        lblMessage.setText(
    	                "Veuillez sélectionner la date de retour."
    	        );

    	        return;
    	    }

    	    String dateRetourReelle =
    	            txtDateRetourReelle.getValue().toString();

    	    if (dateRetourReelle.isEmpty()) {

    	        lblMessage.setText(
    	                "Veuillez saisir la date de retour."
    	        );

    	        return;
    	    }


        try {


            //String dateRetourReelle =
                   // txtDateRetourReelle.getText();

            empruntDAO.enregistrerRetour(
                    idEmprunt,
                    dateRetourReelle
            );

            lblMessage.setText(
                    "Retour enregistré avec succès !"
            );

            // Actualiser le tableau
            afficherEmprunts();

            afficherEmprunts();

            cmbEmpruntRetour.getSelectionModel().clearSelection();

            txtDateRetourReelle.setValue(null);

            chargerEmpruntsPourRetour();

            chargerLivresDisponibles();

        } catch (Exception e) {

            e.printStackTrace();

            lblMessage.setText(
                    "Erreur lors de l'enregistrement du retour."
            );
        }
    }
    public void setUtilisateur(int idUtilisateur, String role) {

        this.idUtilisateur = idUtilisateur;
        this.role = role;

        System.out.println(
                "Utilisateur reçu : ID = "
                + idUtilisateur
                + ", rôle = "
                + role
        );
    }
    private void chargerLivresDisponibles() {

        livresIds = empruntDAO.getLivresDisponibles();

        cmbLivre.getItems().clear();

        for (String livre : livresIds.keySet()) {
            cmbLivre.getItems().add(livre);
        }
    }
    private void chargerAdherents() {

        adherentsIds = empruntDAO.getAdherents();

        cmbAdherent.getItems().clear();

        for (String adherent : adherentsIds.keySet()) {
            cmbAdherent.getItems().add(adherent);
        }
    }
    @FXML
    private void retourAuTableauDeBord(ActionEvent event) {

        Stage fenetreActuelle =
                (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

        fenetreActuelle.close();
    }
}
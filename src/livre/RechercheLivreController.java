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
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class RechercheLivreController {

    @FXML
    private TextField txtRecherche;

    @FXML
    private FlowPane grilleLivres;

    @FXML
    private Label lblMessage;

    private int idUtilisateurConnecte = -1;
    private String roleConnecte = "";


    @FXML
    public void initialize() {
        txtRecherche.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                grilleLivres.getChildren().clear();
                lblMessage.setText("Saisissez un titre, un auteur ou une catégorie pour rechercher un livre.");
                return;
            }
            rechercher();
        });

        grilleLivres.getChildren().clear();
        lblMessage.setText("Saisissez un titre, un auteur ou une catégorie pour rechercher un livre.");
    }


    public void setUtilisateur(int idUtilisateur, String role) {
        this.idUtilisateurConnecte = idUtilisateur;
        this.roleConnecte = role == null ? "" : role;
        CartesLivres.setContext(idUtilisateurConnecte, roleConnecte);
    }


    public void setRole(String role) {
        this.roleConnecte = role == null ? "" : role;
        CartesLivres.setContext(idUtilisateurConnecte, roleConnecte);
    }


    @FXML
    private void rechercher() {
        String recherche = txtRecherche.getText() == null ? "" : txtRecherche.getText().trim();


        if (recherche.isEmpty()) {
            grilleLivres.getChildren().clear();
            lblMessage.setText("Saisissez un titre, un auteur ou une catégorie pour rechercher un livre.");
            return;
        }


        String sql =
                "SELECT id_livre, titre, auteur, categorie, nombre_exemplaires, nombre_disponible, "
              + "isbn, editeur, date_publication FROM livre "
              + "WHERE titre LIKE ? OR auteur LIKE ? OR categorie LIKE ? ORDER BY titre";


        try (var connexion = Connexion.getConnection();
             var ps = connexion.prepareStatement(sql)) {

            String mot = "%" + recherche + "%";
            ps.setString(1, mot);
            ps.setString(2, mot);
            ps.setString(3, mot);


            try (var rs = ps.executeQuery()) {
                grilleLivres.getChildren().clear();
                int count = 0;


                while (rs.next()) {
                    ObservableList<String> ligne = FXCollections.observableArrayList();
                    ligne.add(rs.getString("id_livre"));
                    ligne.add(rs.getString("titre"));
                    ligne.add(rs.getString("auteur"));
                    ligne.add(rs.getString("categorie"));
                    ligne.add(rs.getString("nombre_exemplaires"));
                    ligne.add(rs.getString("nombre_disponible"));
                    ligne.add(rs.getString("isbn"));
                    ligne.add(rs.getString("editeur"));
                    ligne.add(rs.getString("date_publication"));


                    grilleLivres.getChildren().add(CartesLivres.creer(ligne));
                    count++;
                }


                if (count == 0) {
                    lblMessage.setText("Aucun livre trouvé pour : " + recherche);
                } else {
                    lblMessage.setText(count + " livre(s) trouvé(s) pour : " + recherche);
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
            lblMessage.setText("Erreur lors de la recherche.");
        }
    }


    @FXML
    private void afficherTousLesLivres() {
        chargerLivres();
    }


    private void chargerLivres() {
        String sql =
                "SELECT id_livre, titre, auteur, categorie, nombre_exemplaires, nombre_disponible, "
              + "isbn, editeur, date_publication FROM livre ORDER BY titre";


        try (var connexion = Connexion.getConnection();
             var ps = connexion.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            grilleLivres.getChildren().clear();
            int count = 0;


            while (rs.next()) {
                ObservableList<String> ligne = FXCollections.observableArrayList();
                ligne.add(rs.getString("id_livre"));
                ligne.add(rs.getString("titre"));
                ligne.add(rs.getString("auteur"));
                ligne.add(rs.getString("categorie"));
                ligne.add(rs.getString("nombre_exemplaires"));
                ligne.add(rs.getString("nombre_disponible"));
                ligne.add(rs.getString("isbn"));
                ligne.add(rs.getString("editeur"));
                ligne.add(rs.getString("date_publication"));


                grilleLivres.getChildren().add(CartesLivres.creer(ligne));
                count++;
            }


            lblMessage.setText(count + " livre(s) disponible(s) dans le catalogue.");


        } catch (SQLException e) {
            e.printStackTrace();
            lblMessage.setText("Erreur lors du chargement des livres.");
        }
    }


    @FXML
    private void retourAuTableauDeBord(ActionEvent event) {
        Stage fenetreActuelle = (Stage) ((Node) event.getSource()).getScene().getWindow();
        fenetreActuelle.close();
    }
}
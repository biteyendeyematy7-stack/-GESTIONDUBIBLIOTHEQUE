package livre;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Construit la "carte" d'un adhérent : avatar avec initiales, nom,
 * identifiant, téléphone, e-mail et date d'inscription.
 *
 * Une ligne contient, dans cet ordre :
 * 0 id, 1 nom, 2 prénom, 3 téléphone, 4 e-mail,
 * 5 date d'inscription, 6 identifiant, 7 adresse.
 */
public final class CartesAdherents {

    // Couleurs d'avatar : choisies de façon stable à partir du nom
    private static final String[] COULEURS = {
            "#6B4430", // brun
            "#7B2D3B", // bordeaux
            "#2F5D46", // vert forêt
            "#8A5A12", // ambre foncé
            "#A24F1D", // terracotta foncé
            "#7A3E22"  // rouille
    };

    private static final double LARGEUR = 310;

    private CartesAdherents() {
    }

    public static VBox creer(ObservableList<String> ligne) {

        String nom = texte(ligne, 1, "");
        String prenom = texte(ligne, 2, "");
        String telephone = texte(ligne, 3, "Non renseigné");
        String email = texte(ligne, 4, "Non renseigné");
        String date = texte(ligne, 5, "");
        String login = texte(ligne, 6, "");
        String adresse = texte(ligne, 7, "");

        String nomComplet = (prenom + " " + nom).trim();

        if (nomComplet.isEmpty()) {
            nomComplet = "Adhérent sans nom";
        }

        // ---------- En-tête : avatar + nom + identifiant ----------
        Label avatar = new Label(initiales(prenom, nom));
        avatar.getStyleClass().add("adherent-avatar");
        avatar.setStyle("-fx-background-color: " + couleur(nomComplet) + ";");

        Label lblNom = new Label(nomComplet);
        lblNom.getStyleClass().add("adherent-nom");
        lblNom.setMaxWidth(170);

        VBox bloc = new VBox(5, lblNom);

        if (!login.isEmpty()) {

            Label lblLogin = new Label(login);
            lblLogin.getStyleClass().add("adherent-login");

            HBox ligneLogin = new HBox(lblLogin);
            ligneLogin.setAlignment(Pos.CENTER_LEFT);

            bloc.getChildren().add(ligneLogin);
        }

        Region espace = new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);

        HBox entete = new HBox(14, avatar, bloc, espace);
        entete.setAlignment(Pos.CENTER_LEFT);

        if (estRecent(date)) {

            Label nouveau = new Label("Nouveau");
            nouveau.getStyleClass().add("badge-nouveau");
            entete.getChildren().add(nouveau);
        }

        // ---------- Séparateur ----------
        Region separateur = new Region();
        separateur.getStyleClass().add("adherent-separateur");
        separateur.setMinHeight(1);
        separateur.setPrefHeight(1);
        separateur.setMaxHeight(1);
        separateur.setMaxWidth(Double.MAX_VALUE);

        // ---------- Coordonnées ----------
        String texteDate = date.isEmpty()
                ? "Date d'inscription inconnue"
                : "Inscrit le " + formaterDate(date);

        VBox infos = new VBox(7,
                ligneInfo("📞", telephone),
                ligneInfo("✉", email),
                ligneInfo("📅", texteDate));

        // ---------- Carte complète ----------
        VBox carte = new VBox(12, entete, separateur, infos);
        carte.getStyleClass().add("adherent-carte");
        carte.setMinWidth(LARGEUR);
        carte.setPrefWidth(LARGEUR);
        carte.setMaxWidth(LARGEUR);

        String detail = nomComplet
                + (login.isEmpty() ? "" : "\nIdentifiant : " + login)
                + (adresse.isEmpty() ? "" : "\nAdresse : " + adresse);

        Tooltip.install(carte, new Tooltip(detail));

        return carte;
    }

    private static HBox ligneInfo(String icone, String valeur) {

        Label lblIcone = new Label(icone);
        lblIcone.getStyleClass().add("adherent-icone");
        lblIcone.setMinWidth(22);

        Label lblTexte = new Label(valeur);
        lblTexte.getStyleClass().add("adherent-info");
        lblTexte.setMaxWidth(235);

        HBox ligne = new HBox(8, lblIcone, lblTexte);
        ligne.setAlignment(Pos.CENTER_LEFT);

        return ligne;
    }

    private static String initiales(String prenom, String nom) {

        String resultat = "";

        if (!prenom.isBlank()) {
            resultat += prenom.substring(0, 1);
        }

        if (!nom.isBlank()) {
            resultat += nom.substring(0, 1);
        }

        return resultat.isEmpty() ? "?" : resultat.toUpperCase();
    }

    private static String couleur(String nomComplet) {

        int index = Math.floorMod(
                nomComplet.trim().toLowerCase().hashCode(), COULEURS.length);

        return COULEURS[index];
    }

    // Inscrit depuis moins de 30 jours
    private static boolean estRecent(String date) {

        try {

            LocalDate inscription =
                    LocalDate.parse(date.trim().substring(0, 10));

            long jours = ChronoUnit.DAYS.between(inscription, LocalDate.now());

            return jours >= 0 && jours <= 30;

        } catch (Exception e) {

            return false;
        }
    }

    // Exemple : 2026-09-13 devient 13 septembre 2026
    private static String formaterDate(String brut) {

        try {

            return LocalDate.parse(brut.trim().substring(0, 10))
                    .format(DateTimeFormatter.ofPattern(
                            "d MMMM yyyy", Locale.FRENCH));

        } catch (Exception e) {

            return brut;
        }
    }

    private static String texte(
            ObservableList<String> ligne, int index, String parDefaut) {

        if (ligne == null || ligne.size() <= index) {
            return parDefaut;
        }

        String valeur = ligne.get(index);

        if (valeur == null || valeur.isBlank()) {
            return parDefaut;
        }

        return valeur.trim();
    }
}

package livre;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * Construit la "carte" d'un livre : une couverture avec tranche,
 * le titre, l'auteur et un badge de disponibilité.
 *
 * Une ligne contient, dans cet ordre :
 * 0 id, 1 titre, 2 auteur, 3 catégorie, 4 exemplaires,
 * 5 disponibles, 6 ISBN, 7 éditeur, 8 date de publication.
 */
public final class CartesLivres {

    // Couleurs de couverture : une par catégorie (choisie de façon stable)
    private static final String[] COULEURS = {
            "#6B4430", // brun
            "#7B2D3B", // bordeaux
            "#2F5D46", // vert forêt
            "#8A5A12", // ambre foncé
            "#A24F1D", // terracotta foncé
            "#7A3E22"  // rouille
    };

    private static final double LARGEUR = 176;
    private static final double HAUTEUR_COUVERTURE = 216;

    private CartesLivres() {
    }

    public static VBox creer(ObservableList<String> ligne) {

        String titre = texte(ligne, 1, "Sans titre");
        String auteur = texte(ligne, 2, "Auteur inconnu");
        String categorie = texte(ligne, 3, "Divers");
        int exemplaires = entier(ligne, 4);
        int disponibles = entier(ligne, 5);

        // ---------- Couverture ----------
        VBox couverture = creerCouverture(titre, auteur, categorie);

        // ---------- Disponibilité ----------
        Label badge;

        if (disponibles > 0) {
            badge = new Label("Disponible · " + disponibles + "/" + exemplaires);
            badge.getStyleClass().addAll("badge-livre", "badge-dispo");
        } else {
            badge = new Label("Indisponible");
            badge.getStyleClass().addAll("badge-livre", "badge-indispo");
        }

        // ---------- Carte complète ----------
        VBox carte = new VBox(10, couverture, badge);
        carte.setAlignment(Pos.TOP_CENTER);
        carte.getStyleClass().add("livre-carte");
        carte.setMinWidth(LARGEUR);
        carte.setPrefWidth(LARGEUR);
        carte.setMaxWidth(LARGEUR);

        String detail = titre
                + "\n" + auteur
                + "\nCatégorie : " + categorie
                + "\nÉditeur : " + texte(ligne, 7, "non renseigné")
                + "\nISBN : " + texte(ligne, 6, "non renseigné")
                + "\nPublication : " + texte(ligne, 8, "non renseignée");

        Tooltip.install(carte, new Tooltip(detail));

        return carte;
    }

    /**
     * Crée la couverture seule afin qu'elle puisse aussi être réutilisée
     * dans l'historique des emprunts, avec le même style que le catalogue.
     */
    public static VBox creerCouverture(
            String titre,
            String auteur,
            String categorie) {

        titre = titre == null || titre.isBlank() ? "Sans titre" : titre.trim();
        auteur = auteur == null || auteur.isBlank()
                ? "Auteur inconnu"
                : auteur.trim();
        categorie = categorie == null || categorie.isBlank()
                ? "Divers"
                : categorie.trim();

        Label lblCategorie = new Label(categorie);
        lblCategorie.getStyleClass().add("couv-categorie");
        lblCategorie.setMaxWidth(Double.MAX_VALUE);
        lblCategorie.setAlignment(Pos.CENTER);

        Label lblTitre = new Label(titre);
        lblTitre.getStyleClass().add("couv-titre");
        lblTitre.setWrapText(true);
        lblTitre.setTextAlignment(TextAlignment.CENTER);
        lblTitre.setAlignment(Pos.CENTER);
        lblTitre.setMaxWidth(Double.MAX_VALUE);

        Region filet = new Region();
        filet.getStyleClass().add("couv-filet");
        filet.setMinSize(40, 2);
        filet.setPrefSize(40, 2);
        filet.setMaxSize(40, 2);

        Label lblAuteur = new Label(auteur);
        lblAuteur.getStyleClass().add("couv-auteur");
        lblAuteur.setWrapText(true);
        lblAuteur.setTextAlignment(TextAlignment.CENTER);
        lblAuteur.setAlignment(Pos.CENTER);
        lblAuteur.setMaxWidth(Double.MAX_VALUE);

        Region espaceHaut = new Region();
        VBox.setVgrow(espaceHaut, Priority.ALWAYS);

        Region espaceBas = new Region();
        VBox.setVgrow(espaceBas, Priority.ALWAYS);

        VBox couverture = new VBox(8,
                lblCategorie, espaceHaut, lblTitre, filet, lblAuteur, espaceBas);
        couverture.setAlignment(Pos.TOP_CENTER);
        couverture.getStyleClass().add("livre-couverture");
        couverture.setMinSize(LARGEUR, HAUTEUR_COUVERTURE);
        couverture.setPrefSize(LARGEUR, HAUTEUR_COUVERTURE);
        couverture.setMaxSize(LARGEUR, HAUTEUR_COUVERTURE);
        couverture.setStyle(fondCouverture(categorie));

        return couverture;
    }

    // Couleur de fond + tranche sombre + liseré doré près de la tranche
    private static String fondCouverture(String categorie) {

        int index = Math.floorMod(
                categorie.trim().toLowerCase().hashCode(), COULEURS.length);

        return "-fx-background-color: "
                + "linear-gradient(to right, rgba(0,0,0,0.32) 0px, rgba(0,0,0,0.0) 18px), "
                + "linear-gradient(to right, transparent 0px, transparent 21px, "
                + "rgba(229,204,151,0.60) 21px, rgba(229,204,151,0.60) 22px, "
                + "transparent 22px), "
                + COULEURS[index] + ";";
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

    private static int entier(ObservableList<String> ligne, int index) {

        try {
            return Integer.parseInt(texte(ligne, index, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

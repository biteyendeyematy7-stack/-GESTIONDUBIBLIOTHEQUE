package livre;

public class TestRetour {
	public static void main(String[] args) {

        EmpruntDAO empruntDAO = new EmpruntDAO();

        empruntDAO.enregistrerRetour(
                1,              // ID de l'emprunt
                "2026-09-15"    // Date du retour réel
        );

        System.out.println("Test du retour terminé !");
    }

}

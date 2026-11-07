package livre;

public class TestEmpruntDAO {
	

	    public static void main(String[] args) {

	        EmpruntDAO empruntDAO = new EmpruntDAO();

	        empruntDAO.ajouterEmprunt(
	                1,              // ID du livre
	                1,              // ID de l'adhérent
	                2,              // ID de l'utilisateur
	                "2026-09-11",   // Date d'emprunt
	                "2026-09-25"    // Date de retour prévue
	        );

	        System.out.println("Test terminé !");
	    }

}

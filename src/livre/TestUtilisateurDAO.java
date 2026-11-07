package livre;

public class TestUtilisateurDAO {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 String role = UtilisateurDAO.verifierConnexion("admin", "1234");

	        if (role != null) {
	            System.out.println("Connexion réussie !");
	            System.out.println("Rôle : " + role);
	        } else {
	            System.out.println("Login ou mot de passe incorrect !");
	        }

	}

}

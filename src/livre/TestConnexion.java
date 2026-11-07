package livre;
import java.sql.Connection;

public class TestConnexion {
	public static void main(String[] args) {

        Connection connexion = Connexion.getConnection();

        if (connexion != null) {
            System.out.println("La base bibliotheque est accessible !");
        } else {
            System.out.println("La connexion a échoué !");
        }
    }

}

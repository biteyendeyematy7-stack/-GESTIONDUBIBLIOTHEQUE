package livre;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connexion {
	private static final String URL =
            "jdbc:mysql://localhost:3306/bibliotheque";

    private static final String USER =
            "root";

    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion réussie !");
            return connexion;

        } catch (ClassNotFoundException e) {
            System.out.println("Pilote MySQL introuvable. Vérifie la dépendance JDBC.");
            e.printStackTrace();
            return null;

        } catch (SQLException e) {
            System.out.println("Erreur de connexion à MySQL !");
            e.printStackTrace();
            return null;
        }
    }

}
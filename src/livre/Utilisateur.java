package livre;

public class Utilisateur {
	

	    private int idUtilisateur;
	    private String login;
	    private String role;

	    public Utilisateur(int idUtilisateur, String login, String role) {
	        this.idUtilisateur = idUtilisateur;
	        this.login = login;
	        this.role = role;
	    }

	    public int getIdUtilisateur() {
	        return idUtilisateur;
	    }

	    public String getLogin() {
	        return login;
	    }

	    public String getRole() {
	        return role;
	    }
	}



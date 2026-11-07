module GESTIONDUBIBLIOTHEQUE {
	requires java.sql;
	 requires javafx.controls;
	    requires javafx.fxml;
	    opens livre to javafx.fxml;
	    exports livre;
}
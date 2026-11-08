package livre;

public class TestFluxBibliotheque {

    public static void main(String[] args) {
        System.out.println("=== Test du flux bibliothèque ===");

        DemandeEmpruntController demandeCtrl = new DemandeEmpruntController();
        ReservationController reservationCtrl = new ReservationController();
        SuspensionController suspensionCtrl = new SuspensionController();
        NotificationController notificationCtrl = new NotificationController();

        System.out.println("1) Demande d'emprunt pour livre disponible");
        System.out.println(demandeCtrl.demanderLivre(3, 3));

        System.out.println("2) Vérification suspension");
        System.out.println("Suspendu ? " + suspensionCtrl.adherentEstSuspendu(3));

        System.out.println("3) Réservation pour livre indisponible");
        System.out.println(reservationCtrl.reserverLivre(1, 7));

        System.out.println("4) Notification de test");
        System.out.println(notificationCtrl.envoyerNotification(3, "test", "Notification de test envoyée."));

        System.out.println("5) Vérification du cycle retour / retard");
        EmpruntDAO empruntDAO = new EmpruntDAO();
        System.out.println("Retard pour emprunt 1 : " + empruntDAO.calculerRetardJours(1) + " jour(s)");

        System.out.println("=== Fin du test ===");
    }
}
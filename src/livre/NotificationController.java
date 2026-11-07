package livre;

public class NotificationController {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    public String envoyerNotification(int idAdherent, String type, String message) {
        if (idAdherent <= 0 || type == null || type.trim().isEmpty() || message == null || message.trim().isEmpty()) {
            return "Informations de notification incomplètes.";
        }

        boolean ok = notificationDAO.ajouterNotification(idAdherent, type, message);
        if (!ok) {
            return "Erreur lors de l’envoi de la notification.";
        }

        return "Notification envoyée.";
    }
}

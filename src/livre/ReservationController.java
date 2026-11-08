package livre;

public class ReservationController {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public String reserverLivre(int idLivre, int idAdherent) {
        if (idLivre <= 0 || idAdherent <= 0) {
            return "Données invalides.";
        }

        boolean ok = reservationDAO.reserver(idLivre, idAdherent);
        if (!ok) {
            return "Erreur lors de l’enregistrement de la réservation.";
        }

        notificationDAO.ajouterNotification(
                idAdherent,
                "reservation",
                "Votre réservation a été enregistrée. Vous serez averti lorsqu’un exemplaire sera disponible."
        );

        return "Réservation enregistrée avec succès.";
    }

    public String notifierLivreDisponible(int idLivre) {
        if (idLivre <= 0) {
            return "Livre invalide.";
        }

        boolean ok = reservationDAO.notifierProchaineReservation(idLivre);
        if (!ok) {
            return "Aucune réservation en attente pour ce livre.";
        }

        return "Le prochain adhérent a été notifié.";
    }

    public String confirmerRetraitReservation(int idReservation, int idUtilisateur) {
        if (idReservation <= 0 || idUtilisateur <= 0) {
            return "Paramètres invalides.";
        }

        boolean ok = reservationDAO.confirmerRetraitReservation(idReservation, idUtilisateur);
        if (!ok) {
            return "Impossible de confirmer la remise du livre réservé.";
        }

        return "Remise confirmée pour cette réservation.";
    }

    public String annulerReservation(int idReservation, int idAdherent) {
        if (idReservation <= 0 || idAdherent <= 0) {
            return "Paramètres invalides.";
        }

        if (!reservationDAO.reservationPeutEtreAnnulee(idReservation, idAdherent)) {
            return "Cette réservation ne peut plus être annulée.";
        }

        boolean ok = reservationDAO.annulerReservation(idReservation, idAdherent);
        if (!ok) {
            return "Impossible d’annuler cette réservation.";
        }

        notificationDAO.ajouterNotification(
                idAdherent,
                "annulation_reservation",
                "Votre réservation a bien été annulée."
        );

        return "Réservation annulée avec succès.";
    }

    public String annulerReservationBibliothecaire(int idReservation) {
        if (idReservation <= 0) {
            return "Réservation invalide.";
        }

        boolean ok = reservationDAO.annulerReservationBibliothecaire(idReservation);
        if (!ok) {
            return "Impossible d’annuler cette réservation.";
        }

        return "Réservation annulée. La file d’attente a été mise à jour.";
    }
}
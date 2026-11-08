package livre;

public class DemandeEmpruntController {

    private final DemandeEmpruntDAO demandeEmpruntDAO = new DemandeEmpruntDAO();
    private final SuspensionDAO suspensionDAO = new SuspensionDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public String demanderLivre(int idLivre, int idAdherent) {
        if (idLivre <= 0 || idAdherent <= 0) {
            return "Données invalides.";
        }

        if (suspensionDAO.adherentEstSuspendu(idAdherent)) {
            return "L’adhérent est suspendu. Demande refusée.";
        }

        if (!demandeEmpruntDAO.peutFaireNouvelEmprunt(idAdherent)) {
            return "Refus : vous avez déjà 2 emprunts en cours. Vous ne pouvez faire un nouveau prêt qu’après avoir rendu au moins l’un de vos emprunts actuels.";
        }

        if (demandeEmpruntDAO.aUnEmpruntEnCours(idLivre, idAdherent)) {
            return "Refus : vous avez déjà ce livre en cours d’emprunt et il n’a pas encore été rendu. Veuillez d’abord retourner le livre avant de faire une nouvelle demande.";
        }

        boolean ok = demandeEmpruntDAO.demander(idLivre, idAdherent);
        if (!ok) {
            return "Erreur lors de l’enregistrement de la demande.";
        }

        notificationDAO.ajouterNotification(
                idAdherent,
                "demande_emprunt",
                "Votre demande d’emprunt a bien été enregistrée. En attente de validation."
        );

        return "Demande d’emprunt enregistrée avec succès.";
    }

    public String accepterDemande(int idDemande, int idUtilisateurDecision) {
        if (idDemande <= 0 || idUtilisateurDecision <= 0) {
            return "Paramètres invalides.";
        }

        boolean ok = demandeEmpruntDAO.accepter(idDemande, idUtilisateurDecision);
        if (!ok) {
            return "Impossible d’accepter cette demande.";
        }

        return "Demande acceptée : le livre doit être retiré.";
    }

    public String refuserDemande(int idDemande, int idUtilisateurDecision, String motif) {
        if (idDemande <= 0 || idUtilisateurDecision <= 0 || motif == null || motif.trim().isEmpty()) {
            return "Motif de refus obligatoire.";
        }

        boolean ok = demandeEmpruntDAO.refuser(idDemande, idUtilisateurDecision, motif);
        if (!ok) {
            return "Impossible de refuser cette demande.";
        }

        return "Demande refusée.";
    }

    public String confirmerRetrait(int idDemande, int idUtilisateur) {
        if (idDemande <= 0 || idUtilisateur <= 0) {
            return "Paramètres invalides.";
        }

        boolean ok = demandeEmpruntDAO.confirmerRetrait(idDemande, idUtilisateur);
        if (!ok) {
            return "Impossible de confirmer le retrait. Vérifiez la demande et la disponibilité.";
        }

        return "Retrait confirmé. L’emprunt réel a été créé.";
    }

    public String confirmerRemiseDemande(int idDemande, int idUtilisateur) {
        if (idDemande <= 0 || idUtilisateur <= 0) {
            return "Paramètres invalides.";
        }

        boolean ok = demandeEmpruntDAO.confirmerRetrait(idDemande, idUtilisateur);
        if (!ok) {
            return "Impossible de confirmer la remise. Vérifiez la demande et l’état du livre.";
        }

        return "Remise confirmée. L’emprunt réel a été créé.";
    }

    public String annulerDemande(int idDemande, int idAdherent) {
        if (idDemande <= 0 || idAdherent <= 0) {
            return "Paramètres invalides.";
        }

        if (!demandeEmpruntDAO.demandePeutEtreAnnulee(idDemande, idAdherent)) {
            return "Cette demande ne peut plus être annulée.";
        }

        boolean ok = demandeEmpruntDAO.annulerDemande(idDemande, idAdherent);
        if (!ok) {
            return "Impossible d’annuler cette demande.";
        }

        notificationDAO.ajouterNotification(
                idAdherent,
                "annulation_demande",
                "Votre demande d’emprunt a bien été annulée."
        );

        return "Demande annulée avec succès.";
    }
}
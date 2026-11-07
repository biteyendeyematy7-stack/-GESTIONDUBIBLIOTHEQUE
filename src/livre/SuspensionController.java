package livre;

public class SuspensionController {

    private final SuspensionDAO suspensionDAO = new SuspensionDAO();

    public String ajouterSuspension(int idAdherent, int idUtilisateur, String motif, String dateDebut, String dateFin) {
        if (idAdherent <= 0 || idUtilisateur <= 0 || motif == null || motif.trim().isEmpty()) {
            return "Informations de suspension incomplètes.";
        }

        boolean ok = suspensionDAO.ajouterSuspension(idAdherent, idUtilisateur, motif, dateDebut, dateFin);
        if (!ok) {
            return "Erreur lors de la suspension.";
        }

        return "Suspension enregistrée.";
    }

    public boolean adherentEstSuspendu(int idAdherent) {
        return suspensionDAO.adherentEstSuspendu(idAdherent);
    }

    public String leverSuspension(int idAdherent) {
        if (idAdherent <= 0) {
            return "Adhérent invalide.";
        }

        boolean ok = suspensionDAO.leverSuspension(idAdherent);
        if (!ok) {
            return "Aucune suspension active à lever.";
        }

        return "Suspension levée.";
    }
}

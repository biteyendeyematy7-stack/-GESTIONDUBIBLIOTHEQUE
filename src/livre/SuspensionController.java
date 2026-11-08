package livre;

public class SuspensionController {

    private final SuspensionDAO suspensionDAO = new SuspensionDAO();

    public String ajouterSuspension(int idAdherent, int idUtilisateur, String motif, String dateDebut, String dateFin) {
        if (idAdherent <= 0 || idUtilisateur <= 0 || motif == null || motif.trim().isEmpty()) {
            return "Informations de suspension incomplètes.";
        }

        if (!suspensionDAO.estRoleAutorise(idUtilisateur)) {
            return "Seuls l’administrateur et le bibliothécaire peuvent modifier une suspension.";
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

    public String leverSuspension(int idAdherent, int idUtilisateur) {
        if (idAdherent <= 0 || idUtilisateur <= 0) {
            return "Adhérent invalide.";
        }

        if (!suspensionDAO.estRoleAutorise(idUtilisateur)) {
            return "Seuls l’administrateur et le bibliothécaire peuvent modifier une suspension.";
        }

        boolean ok = suspensionDAO.leverSuspension(idAdherent, idUtilisateur);
        if (!ok) {
            return "Aucune suspension active à lever.";
        }

        return "Suspension levée.";
    }

    public String leverSuspension(int idAdherent) {
        return leverSuspension(idAdherent, 1);
    }
}
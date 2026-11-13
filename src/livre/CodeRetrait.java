package livre;

import java.time.LocalDateTime;

public class CodeRetrait {

    private final int idCodeRetrait;
    private final String code;
    private final Integer idDemande;
    private final Integer idReservation;
    private final LocalDateTime dateGeneration;
    private final LocalDateTime dateExpiration;
    private final boolean utilise;
    private final LocalDateTime dateUtilisation;

    public CodeRetrait(
            int idCodeRetrait,
            String code,
            Integer idDemande,
            Integer idReservation,
            LocalDateTime dateGeneration,
            LocalDateTime dateExpiration,
            boolean utilise,
            LocalDateTime dateUtilisation) {

        this.idCodeRetrait = idCodeRetrait;
        this.code = code;
        this.idDemande = idDemande;
        this.idReservation = idReservation;
        this.dateGeneration = dateGeneration;
        this.dateExpiration = dateExpiration;
        this.utilise = utilise;
        this.dateUtilisation = dateUtilisation;
    }

    public int getIdCodeRetrait() {
        return idCodeRetrait;
    }

    public String getCode() {
        return code;
    }

    public Integer getIdDemande() {
        return idDemande;
    }

    public Integer getIdReservation() {
        return idReservation;
    }

    public LocalDateTime getDateGeneration() {
        return dateGeneration;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public boolean isUtilise() {
        return utilise;
    }

    public LocalDateTime getDateUtilisation() {
        return dateUtilisation;
    }

    public boolean estExpire() {
        return dateExpiration == null || !LocalDateTime.now().isBefore(dateExpiration);
    }

    public boolean estValide() {
        return !utilise && !estExpire();
    }
}

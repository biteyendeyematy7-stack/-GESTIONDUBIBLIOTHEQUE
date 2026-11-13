package livre;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class CodeRetraitDAO {

    private static final int LONGUEUR_CODE = 8;
    private static final int NOMBRE_TENTATIVES_UNICITE = 20;
    private static final SecureRandom RANDOM = new SecureRandom();

    public CodeRetrait genererCodePourDemande(int idDemande, int dureeValiditeJours) {
        if (idDemande <= 0 || dureeValiditeJours <= 0) {
            return null;
        }

        try (Connection connexion = Connexion.getConnection()) {
            return genererCodePourDemande(connexion, idDemande, dureeValiditeJours);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public CodeRetrait genererCodePourReservation(int idReservation, int dureeValiditeJours) {
        if (idReservation <= 0 || dureeValiditeJours <= 0) {
            return null;
        }

        try (Connection connexion = Connexion.getConnection()) {
            return genererCodePourReservation(connexion, idReservation, dureeValiditeJours);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    CodeRetrait genererCodePourDemande(
            Connection connexion,
            int idDemande,
            int dureeValiditeJours) throws SQLException {

        if (connexion == null || idDemande <= 0 || dureeValiditeJours <= 0) {
            return null;
        }

        CodeRetrait existant = rechercherActifPourDemande(connexion, idDemande);
        if (existant != null) {
            return existant;
        }

        LocalDateTime expiration = LocalDateTime.now().plusDays(dureeValiditeJours);
        String sql =
                "INSERT INTO code_retrait "
              + "(code, id_demande, date_expiration, utilise) "
              + "VALUES (?, ?, ?, 0)";

        return insererCodeUnique(connexion, sql, idDemande, null, expiration);
    }

    CodeRetrait genererCodePourReservation(
            Connection connexion,
            int idReservation,
            int dureeValiditeJours) throws SQLException {

        if (connexion == null || idReservation <= 0 || dureeValiditeJours <= 0) {
            return null;
        }

        CodeRetrait existant = rechercherActifPourReservation(connexion, idReservation);
        if (existant != null) {
            return existant;
        }

        LocalDateTime expiration = LocalDateTime.now().plusDays(dureeValiditeJours);
        String sql =
                "INSERT INTO code_retrait "
              + "(code, id_reservation, date_expiration, utilise) "
              + "VALUES (?, ?, ?, 0)";

        return insererCodeUnique(connexion, sql, null, idReservation, expiration);
    }

    public boolean codeExiste(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM code_retrait WHERE code = ?";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CodeRetrait rechercherParCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        String sql =
                "SELECT id_code_retrait, code, id_demande, id_reservation, "
              + "date_generation, date_expiration, utilise, date_utilisation "
              + "FROM code_retrait WHERE code = ?";

        try (Connection connexion = Connexion.getConnection()) {
            return rechercherParCode(connexion, code);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    CodeRetrait rechercherParCode(Connection connexion, String code) throws SQLException {
        if (connexion == null || code == null || code.isBlank()) {
            return null;
        }

        String sql =
                "SELECT id_code_retrait, code, id_demande, id_reservation, "
              + "date_generation, date_expiration, utilise, date_utilisation "
              + "FROM code_retrait WHERE code = ?";

        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? lireCode(rs) : null;
            }
        }
    }

    public CodeRetrait rechercherCodeValide(String code) {
        CodeRetrait codeRetrait = rechercherParCode(code);
        return codeRetrait != null && codeRetrait.estValide() ? codeRetrait : null;
    }

    public boolean estExpire(String code) {
        CodeRetrait codeRetrait = rechercherParCode(code);
        return codeRetrait == null || codeRetrait.estExpire();
    }

    public boolean estDejaUtilise(String code) {
        CodeRetrait codeRetrait = rechercherParCode(code);
        return codeRetrait != null && codeRetrait.isUtilise();
    }

    public boolean marquerCommeUtilise(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }

        String sql =
                "UPDATE code_retrait SET utilise = 1, date_utilisation = NOW() "
              + "WHERE code = ? AND utilise = 0 AND date_expiration > NOW()";

        try (Connection connexion = Connexion.getConnection();
             PreparedStatement ps = connexion.prepareStatement(sql)) {

            ps.setString(1, code.trim());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private CodeRetrait insererCodeUnique(
            Connection connexion,
            String sql,
            Integer idDemande,
            Integer idReservation,
            LocalDateTime expiration) throws SQLException {

        for (int tentative = 0; tentative < NOMBRE_TENTATIVES_UNICITE; tentative++) {
            String code = genererValeurCode();

            try (PreparedStatement ps = connexion.prepareStatement(sql)) {
                ps.setString(1, code);
                ps.setObject(2, idDemande != null ? idDemande : idReservation);
                ps.setTimestamp(3, Timestamp.valueOf(expiration));

                if (ps.executeUpdate() == 1) {
                    return rechercherParCode(connexion, code);
                }
            } catch (SQLException e) {
                if (!estViolationUnicite(e)) {
                    throw e;
                }
            }
        }

        throw new SQLException("Impossible de générer un code de retrait unique.");
    }

    private String genererValeurCode() {
        int maximum = (int) Math.pow(10, LONGUEUR_CODE);
        return String.format("%0" + LONGUEUR_CODE + "d", RANDOM.nextInt(maximum));
    }

    private boolean estViolationUnicite(SQLException exception) {
        return exception.getErrorCode() == 1062
                || "23000".equals(exception.getSQLState());
    }

    private CodeRetrait rechercherActifPourDemande(Connection connexion, int idDemande)
            throws SQLException {
        String sql =
                "SELECT id_code_retrait, code, id_demande, id_reservation, "
              + "date_generation, date_expiration, utilise, date_utilisation "
              + "FROM code_retrait "
              + "WHERE id_demande = ? AND utilise = 0 AND date_expiration > NOW()";

        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idDemande);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? lireCode(rs) : null;
            }
        }
    }

    private CodeRetrait rechercherActifPourReservation(Connection connexion, int idReservation)
            throws SQLException {
        String sql =
                "SELECT id_code_retrait, code, id_demande, id_reservation, "
              + "date_generation, date_expiration, utilise, date_utilisation "
              + "FROM code_retrait "
              + "WHERE id_reservation = ? AND utilise = 0 AND date_expiration > NOW()";

        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, idReservation);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? lireCode(rs) : null;
            }
        }
    }

    private CodeRetrait lireCode(ResultSet rs) throws SQLException {
        int idDemande = rs.getInt("id_demande");
        Integer demande = rs.wasNull() ? null : idDemande;

        int idReservation = rs.getInt("id_reservation");
        Integer reservation = rs.wasNull() ? null : idReservation;

        Timestamp dateGeneration = rs.getTimestamp("date_generation");
        Timestamp dateExpiration = rs.getTimestamp("date_expiration");
        Timestamp dateUtilisation = rs.getTimestamp("date_utilisation");

        return new CodeRetrait(
                rs.getInt("id_code_retrait"),
                rs.getString("code"),
                demande,
                reservation,
                dateGeneration == null ? null : dateGeneration.toLocalDateTime(),
                dateExpiration == null ? null : dateExpiration.toLocalDateTime(),
                rs.getBoolean("utilise"),
                dateUtilisation == null ? null : dateUtilisation.toLocalDateTime());
    }
}

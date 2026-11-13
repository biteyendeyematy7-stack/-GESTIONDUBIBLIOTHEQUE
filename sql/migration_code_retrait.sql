-- Migration non destructive pour les codes de retrait.
-- A executer sur la base bibliotheque apres verification des tables
-- demande_emprunt et reservation existantes.

CREATE TABLE IF NOT EXISTS code_retrait (
    id_code_retrait INT NOT NULL AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    id_demande INT NULL,
    id_reservation INT NULL,
    date_generation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_expiration DATETIME NOT NULL,
    utilise TINYINT(1) NOT NULL DEFAULT 0,
    date_utilisation DATETIME NULL,

    PRIMARY KEY (id_code_retrait),
    UNIQUE KEY uq_code_retrait_code (code),
    UNIQUE KEY uq_code_retrait_demande (id_demande),
    UNIQUE KEY uq_code_retrait_reservation (id_reservation),
    KEY idx_code_retrait_expiration (date_expiration, utilise),

    CONSTRAINT fk_code_retrait_demande
        FOREIGN KEY (id_demande) REFERENCES demande_emprunt (id_demande),
    CONSTRAINT fk_code_retrait_reservation
        FOREIGN KEY (id_reservation) REFERENCES reservation (id_reservation),
    CONSTRAINT chk_code_retrait_parent CHECK (
        (id_demande IS NOT NULL AND id_reservation IS NULL)
        OR (id_demande IS NULL AND id_reservation IS NOT NULL)
    )
) ENGINE=InnoDB;

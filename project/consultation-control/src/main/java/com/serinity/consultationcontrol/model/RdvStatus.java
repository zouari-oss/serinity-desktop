package com.serinity.consultationcontrol.model;

public enum RdvStatus {
    EN_ATTENTE,
    APPROUVE,
    REFUSE,
    MODIFICATION_PROPOSEE;

    public static RdvStatus fromDatabase(String value) {
        if (value == null || value.isBlank()) {
            return EN_ATTENTE;
        }

        return switch (value.trim().toUpperCase()) {
            case "VALIDE", "VALIDEE", "APPROUVEE" -> APPROUVE;
            case "EN_ATTENTE" -> EN_ATTENTE;
            case "APPROUVE" -> APPROUVE;
            case "REFUSE" -> REFUSE;
            case "MODIFICATION_PROPOSEE" -> MODIFICATION_PROPOSEE;
            default -> EN_ATTENTE;
        };
    }
}

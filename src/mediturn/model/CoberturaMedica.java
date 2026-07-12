package mediturn.model;

/**
 * Representa la cobertura médica del paciente.
 * Se usa un enum porque son valores fijos y conocidos de antemano
 * (a diferencia de, por ejemplo, el nombre de un paciente, que es un dato variable).
 */

public enum CoberturaMedica {
   
    OSDE("OSDE"),
    FEDERADA_SALUD("Federada Salud"),
    SWISS_MEDICAL("Swiss Medical"),
    PARTICULAR("Particular (sin cobertura)");

    private final String nombreLegible;

    CoberturaMedica(String nombreLegible) {
        this.nombreLegible = nombreLegible;
    }

    public String getNombreLegible() {
        return nombreLegible;
    }

}

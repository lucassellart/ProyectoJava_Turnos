package mediturn.model;

/**
 * Enum que representa las especialidades médicas disponibles en la clínica.
 * Al ser un enum, Java garantiza que solo existan estos valores fijos,
 * evitando errores de tipeo o inconsistencias en los datos.
 */

public enum Especialidad {
    
    CLINICA_MEDICA("Clínica Médica"),
    CARDIOLOGIA("Cardiología"),
    PEDIATRIA("Pediatría"),
    TRAUMATOLOGIA("Traumatología"),
    DERMATOLOGIA("Dermatología"),
    GINECOLOGIA("Ginecología");

    // Nombre "legible" para mostrar en el sistema (con tildes y formato correcto)
    private final String nombreLegible;

    // Constructor privado: los enums no se instancian desde afuera,
    // solo se definen los valores fijos de arriba.
    Especialidad(String nombreLegible) {
        this.nombreLegible = nombreLegible;
    }

    public String getNombreLegible() {
        return nombreLegible;
    }

    @Override
    public String toString() {
        return nombreLegible;
    }

}

package mediturn.model;

/**
 * Representa la sucursal (sede) de la clínica donde se atiende el turno.
 *
 * Hoy la clínica solo tiene una sede ("Centro"), por eso el enum
 * tiene un único valor. Se modela igual como enum (y no como texto
 * fijo en el menú) para que el sistema quede preparado para crecer
 * a varias sedes en el futuro sin tener que rediseñar el flujo de
 * turnos: alcanzaría con agregar una constante más acá.
 */

public enum Sucursal {

    CENTRO("Centro");

    private final String nombreLegible;

    Sucursal(String nombreLegible) {
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

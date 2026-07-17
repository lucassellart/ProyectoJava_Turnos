package mediturn.service;

import java.time.LocalDate;
import java.time.LocalTime;
import mediturn.model.Medico;
import mediturn.model.Paciente;
import mediturn.model.Turno;
import mediturn.model.TurnoPresencial;
import mediturn.model.TurnoVirtual;

/**
 * Patrón Factory: centraliza la lógica de "qué subclase de Turno
 * crear" en un solo lugar. Sin esto, cada parte del código que
 * necesita crear un turno tendría que hacer su propio if/else
 * entre TurnoPresencial y TurnoVirtual — repetido y propenso a
 * errores. Con el Factory, GestorClinica solo dice "quiero un
 * turno de tipo X" y no le importa cómo se construye por dentro.
 */

public class TurnoFactory {
    
    public static final String TIPO_PRESENCIAL = "PRESENCIAL";
    public static final String TIPO_VIRTUAL = "VIRTUAL";

    /**
     * Crea un TurnoPresencial o TurnoVirtual según el tipo pedido.
     *
     * @param tipo         "PRESENCIAL" o "VIRTUAL" (no sensible a mayúsculas)
     * @param detalleExtra si es presencial: el consultorio.
     *                     si es virtual: el link de videollamada.
     */
    public static Turno crearTurno(String tipo, LocalDate fecha, LocalTime hora,
                                    Paciente paciente, Medico medico, String detalleExtra) {

        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de turno no puede ser nulo.");
        }

        switch (tipo.toUpperCase()) {
            case TIPO_PRESENCIAL:
                return new TurnoPresencial(fecha, hora, paciente, medico, detalleExtra);
            case TIPO_VIRTUAL:
                return new TurnoVirtual(fecha, hora, paciente, medico, detalleExtra);
            default:
                throw new IllegalArgumentException("Tipo de turno desconocido: " + tipo);
        }
    }

}

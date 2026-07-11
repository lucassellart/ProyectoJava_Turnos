package mediturn.model;

/**
 * Representa el estado actual de un turno médico.
 * Al ser un enum, evitamos usar Strings sueltos como "pendiente"
 * o "cancelado" que podrían escribirse mal en distintas partes del código.
 */
public enum EstadoTurno {
    PENDIENTE,
    CONFIRMADO,
    CANCELADO,
    COMPLETADO
}
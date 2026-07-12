package mediturn.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utilidad para convertir fechas y horas entre LocalDate/LocalTime
 * y String, usando siempre el mismo formato fijo.
 *
 * ¿Por qué no usar directamente fecha.toString()?
 * Porque LocalTime.toString() no siempre devuelve el mismo formato
 * (a veces incluye segundos, a veces no), y eso podría hacer que
 * el parseo de agenda.txt falle de forma inconsistente. Definir
 * un formato explícito y fijo evita ese problema.
 */
public final class FormateadorFecha {

    // DateTimeFormatter es la clase de Java para definir el "molde"
    // con el que se lee/escribe una fecha u hora como texto.
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern(Constantes.PATRON_FECHA);

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern(Constantes.PATRON_HORA);

    private FormateadorFecha() {
    }

    /** Convierte una LocalDate a texto, ej: 2026-07-15 */
    public static String formatearFecha(LocalDate fecha) {
        return fecha.format(FORMATO_FECHA);
    }

    /** Convierte una LocalTime a texto, ej: 14:30 */
    public static String formatearHora(LocalTime hora) {
        return hora.format(FORMATO_HORA);
    }

    /**
     * Convierte texto a LocalDate. Devuelve null si el texto no
     * tiene el formato esperado, en vez de lanzar la excepción hacia
     * arriba, para que quien llame pueda decidir cómo manejar una
     * línea de archivo corrupta sin que el programa se caiga.
     */
    public static LocalDate parsearFecha(String texto) {
        try {
            return LocalDate.parse(texto, FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Igual que parsearFecha, pero para LocalTime. */
    public static LocalTime parsearHora(String texto) {
        try {
            return LocalTime.parse(texto, FORMATO_HORA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
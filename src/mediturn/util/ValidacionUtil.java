package mediturn.util;

/**
 * Utilidad con validaciones simples y reutilizables, para no repetir
 * la misma lógica de chequeo en varias clases de dao/service.
 */
public final class ValidacionUtil {

    private ValidacionUtil() {
    }

    /** true si el texto es null, vacío, o solo espacios. */
    public static boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    /**
     * true si el texto se puede convertir a un número entero positivo.
     * Útil para validar DNI o matrícula antes de hacer Integer.parseInt()
     * y evitar que el programa se caiga con un NumberFormatException.
     */
    public static boolean esEnteroPositivo(String texto) {
        if (estaVacio(texto)) {
            return false;
        }
        try {
            int valor = Integer.parseInt(texto.trim());
            return valor > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * true si una línea leída de agenda.txt/historial.txt tiene
     * exactamente la cantidad de campos esperada. Sirve para
     * detectar líneas corruptas antes de intentar parsearlas.
     */
    public static boolean tieneCantidadDeCampos(String[] campos, int cantidadEsperada) {
        return campos != null && campos.length == cantidadEsperada;
    }
}
package mediturn.util;

/**
 * Utilidad para armar y separar líneas en formato CSV simple
 * (campos separados por Constantes.SEPARADOR).
 *
 * Nota de diseño: un CSV "de verdad" (como el que abrís en Excel)
 * maneja casos complejos como comillas y comas dentro de un campo.
 * Para este proyecto no hace falta esa complejidad: alcanza con
 * evitar que el separador (";") aparezca dentro de un campo de texto
 * libre (como el nombre de un paciente), que es la única forma real
 * de romper el formato.
 */
public final class CsvUtil {

    private CsvUtil() {
    }

    /**
     * Une varios campos en una sola línea, separados por Constantes.SEPARADOR.
     * Ejemplo: unirCampos("Juan", "Perez", "30111222")
     *          -> "Juan;Perez;30111222"
     */
    public static String unirCampos(String... campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            sb.append(sanitizar(campos[i]));
            // No agregamos separador después del último campo
            if (i < campos.length - 1) {
                sb.append(Constantes.SEPARADOR);
            }
        }
        return sb.toString();
    }

    /**
     * Separa una línea del archivo en sus campos originales.
     * Usamos split con límite -1 para que, si el último campo
     * está vacío, igual se conserve como String vacío en el array
     * (por defecto, split() descarta los campos vacíos del final).
     */
    public static String[] dividirLinea(String linea) {
        return linea.split(Constantes.SEPARADOR, -1);
    }

    /**
     * Evita que un campo de texto libre (nombre, consultorio, etc.)
     * contenga el separador, ya que eso correría todos los campos
     * siguientes al leer el archivo. En vez de lanzar una excepción
     * (que sería más estricto), lo reemplazamos por un espacio:
     * es una solución simple y suficiente para este proyecto.
     */
    private static String sanitizar(String campo) {
        if (campo == null) {
            return "";
        }
        return campo.replace(Constantes.SEPARADOR, " ");
    }
}
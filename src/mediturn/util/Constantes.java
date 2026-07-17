package mediturn.util;

/**
 * Clase de constantes globales del proyecto.
 * No se instancia nunca (constructor privado): es solo un lugar
 * centralizado para valores fijos que se usan en varias partes
 * del sistema, para no tener "números/strings mágicos" repetidos
 * y sueltos en el código.
 */
public final class Constantes {

    // Separador de campos usado en agenda.txt e historial.txt
    public static final String SEPARADOR = ";";

    // Rutas de los archivos de persistencia
    public static final String ARCHIVO_AGENDA = "agenda.txt";
    public static final String ARCHIVO_HISTORIAL = "historial.txt";

    public static final String ARCHIVO_PACIENTES = "pacientes.txt";
    public static final String ARCHIVO_MEDICOS = "medicos.txt";

    // Patrones de formato para fechas y horas (ver FormateadorFecha)
    public static final String PATRON_FECHA = "yyyy-MM-dd";
    public static final String PATRON_HORA = "HH:mm";

    // Constructor privado: evita que alguien haga "new Constantes()",
    // ya que no tiene sentido crear una instancia de esta clase.
    private Constantes() {
    }
}

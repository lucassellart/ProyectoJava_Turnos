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

    // Archivos de ejemplo (versionados en git): se usan para precargar
    // pacientes y médicos la primera vez que corre el sistema, cuando
    // pacientes.txt/medicos.txt todavía no existen o están vacíos.
    // Simulan una clínica que "ya tiene gente cargada", como un sistema
    // real (nadie empieza a usar Logimed con la base de pacientes vacía).
    public static final String ARCHIVO_PACIENTES_EJEMPLO = "pacientes-ejemplo.txt";
    public static final String ARCHIVO_MEDICOS_EJEMPLO = "medicos-ejemplo.txt";

    // Patrones de formato para fechas y horas (ver FormateadorFecha)
    public static final String PATRON_FECHA = "yyyy-MM-dd";
    public static final String PATRON_HORA = "HH:mm";

    // Horario de atención de la clínica, usado para generar el calendario
    // de fechas/horarios disponibles al solicitar un turno (en vez de que
    // el paciente escriba fecha/hora "a mano"). Un solo horario fijo para
    // todos los médicos: la clínica no maneja horarios distintos por
    // profesional en este proyecto (alcance definido explícitamente).
    public static final int HORA_APERTURA = 8;   // 8:00
    public static final int HORA_CIERRE = 18;    // 18:00
    public static final int DURACION_TURNO_MINUTOS = 30;

    // Cantidad de días hacia adelante que se ofrecen como fechas
    // disponibles para sacar turno (a partir de mañana).
    public static final int DIAS_DISPONIBILIDAD = 30;

    // Constructor privado: evita que alguien haga "new Constantes()",
    // ya que no tiene sentido crear una instancia de esta clase.
    private Constantes() {
    }
}

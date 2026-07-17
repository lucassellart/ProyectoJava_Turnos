package mediturn.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mediturn.dao.MedicoDAO;
import mediturn.dao.PacienteDAO;
import mediturn.dao.TurnoDAO;
import mediturn.model.CoberturaMedica;
import mediturn.model.Especialidad;
import mediturn.model.EstadoTurno;
import mediturn.model.Medico;
import mediturn.model.MedicoEspecialista;
import mediturn.model.Paciente;
import mediturn.model.Turno;

/**
 * Patrón Singleton: garantiza que exista UNA sola instancia de
 * GestorClinica en todo el programa, y que sea el único punto de
 * entrada para operar sobre pacientes, médicos y turnos.
 *
 * ¿Por qué tiene sentido acá? Porque el estado (quiénes son los
 * pacientes, los médicos, la agenda activa) tiene que ser el mismo
 * sin importar desde qué parte del menú de consola se lo consulte.
 * Si cada clase creara su propio GestorClinica, tendríamos varias
 * "copias" de la clínica funcionando en paralelo sin enterarse
 * una de la otra — un desastre de consistencia.
 *
 * Implementación: Singleton "eager" (la instancia se crea apenas
 * se carga la clase, no en el primer uso). Se elige así por
 * simplicidad: no hay problemas de concurrencia al crearla, y en
 * este proyecto siempre la vamos a necesitar desde el arranque.
 */

public class GestorClinica {
    
    // La única instancia. "static" significa que pertenece a la
    // clase, no a un objeto — por eso se puede acceder sin crear
    // un GestorClinica con "new".
    private static final GestorClinica instancia = new GestorClinica();

    // ---------- estado en memoria ----------
    private Map<Integer, Paciente> pacientes;
    private Map<Integer, Medico> medicos;
    private List<Turno> agenda;      // turnos activos (no completados)
    private List<Turno> historial;   // turnos completados

    // ---------- DAOs (capa de persistencia) ----------
    private final PacienteDAO pacienteDAO;
    private final MedicoDAO medicoDAO;
    private final TurnoDAO turnoDAO;

    // ---------- Observer ----------
    private List<AgendaObserver> observadores;

    /**
     * El constructor es "private": nadie de afuera puede hacer
     * "new GestorClinica()". Es la clave del Singleton — la única
     * forma de conseguir una instancia es a través de getInstancia().
     */
    private GestorClinica() {
        this.pacienteDAO = new PacienteDAO();
        this.medicoDAO = new MedicoDAO();
        this.turnoDAO = new TurnoDAO();
        this.observadores = new ArrayList<>();

        cargarDatosDesdeArchivos();
    }

    /** Punto de acceso único a la instancia. */
    public static GestorClinica getInstancia() {
        return instancia;
    }

    /**
     * Carga todo el estado desde los archivos de texto al arrancar
     * el programa. Se llama una sola vez, desde el constructor.
     */
    private void cargarDatosDesdeArchivos() {
        this.pacientes = pacienteDAO.listarComoMapa();
        this.medicos = medicoDAO.listarComoMapa();
        this.agenda = new ArrayList<>(turnoDAO.listarAgenda(pacientes, medicos));
        this.historial = new ArrayList<>(turnoDAO.listarHistorial(pacientes, medicos));
    }

    // =========================================================
    //  PACIENTES
    // =========================================================

    public Paciente registrarPaciente(int dni, String nombre, String apellido,
                                       String telefono, CoberturaMedica cobertura) {
        if (pacientes.containsKey(dni)) {
            throw new IllegalArgumentException("Ya existe un paciente con DNI " + dni);
        }
        Paciente paciente = new Paciente(dni, nombre, apellido, cobertura, telefono);
        pacientes.put(dni, paciente);
        pacienteDAO.guardar(paciente); // persiste ya mismo (append a pacientes.txt)
        return paciente;
    }

    public Paciente buscarPacientePorDni(int dni) {
        return pacientes.get(dni);
    }

    public List<Paciente> listarPacientes() {
        // Collections.unmodifiableList evita que quien llama a este
        // método modifique la lista interna "por afuera" (rompiendo
        // la encapsulación). Solo GestorClinica puede agregar/quitar.
        return Collections.unmodifiableList(new ArrayList<>(pacientes.values()));
    }

    // =========================================================
    //  MÉDICOS
    // =========================================================

    /** Registra un médico general (sin especialidad principal fija). */
    public Medico registrarMedico(int dni, String nombre, String apellido, String matricula) {
        if (medicos.containsKey(dni)) {
            throw new IllegalArgumentException("Ya existe un médico con DNI " + dni);
        }
        Medico medico = new Medico(dni, nombre, apellido, matricula);
        medicos.put(dni, medico);
        medicoDAO.guardar(medico);
        return medico;
    }

    /** Registra un médico especialista (usa la subclase MedicoEspecialista). */
    public MedicoEspecialista registrarMedicoEspecialista(int dni, String nombre, String apellido,
                                                            String matricula, Especialidad especialidadPrincipal) {
        if (medicos.containsKey(dni)) {
            throw new IllegalArgumentException("Ya existe un médico con DNI " + dni);
        }
        MedicoEspecialista medico = new MedicoEspecialista(dni, nombre, apellido, matricula, especialidadPrincipal);
        medicos.put(dni, medico);
        medicoDAO.guardar(medico);
        return medico;
    }

    public Medico buscarMedicoPorDni(int dni) {
        return medicos.get(dni);
    }

    public List<Medico> listarMedicos() {
        return Collections.unmodifiableList(new ArrayList<>(medicos.values()));
    }

    // =========================================================
    //  TURNOS
    // =========================================================

    /**
     * Solicita un turno nuevo. Usa TurnoFactory para crear el objeto
     * correcto (Presencial/Virtual) y valida que el médico esté
     * libre en ese horario antes de confirmarlo.
     */
    public Turno solicitarTurno(int dniPaciente, int dniMedico, LocalDate fecha,
                                 LocalTime hora, String tipoTurno, String detalleExtra) {

        Paciente paciente = pacientes.get(dniPaciente);
        
        if (paciente == null) {
            throw new IllegalArgumentException("No existe un paciente con DNI " + dniPaciente);
        }
        if (!paciente.isActivo()) {
            throw new IllegalStateException("El paciente con DNI " + dniPaciente + " está dado de baja.");
        }
        
        Medico medico = medicos.get(dniMedico);
        
        if (medico == null) {
            throw new IllegalArgumentException("No existe un médico con DNI " + dniMedico);
        }
        if (!medico.isActivo()) {
            throw new IllegalStateException("El médico con DNI " + dniMedico + " está dado de baja.");
        }
        
        if (haySolapamiento(medico, fecha, hora, null)) {
            throw new IllegalStateException(
                "El médico ya tiene un turno en " + fecha + " a las " + hora);
        }

        Turno turno = TurnoFactory.crearTurno(tipoTurno, fecha, hora, paciente, medico, detalleExtra);
        agenda.add(turno);
        turnoDAO.guardarEnAgenda(turno);
        return turno;
    }

    /**
     * Revisa si el médico ya tiene un turno activo (no cancelado)
     * en esa fecha y hora. "turnoAIgnorar" se usa en reprogramarTurno,
     * para no comparar un turno contra sí mismo.
     */
    private boolean haySolapamiento(Medico medico, LocalDate fecha, LocalTime hora, Turno turnoAIgnorar) {
        for (Turno t : agenda) {
            if (t == turnoAIgnorar) {
                continue;
            }
            if (t.getEstado() == EstadoTurno.CANCELADO) {
                continue; // un turno cancelado no ocupa el horario
            }
            boolean mismoMedico = t.getMedico().getDni() == medico.getDni();
            boolean mismaFechaHora = t.getFecha().equals(fecha) && t.getHora().equals(hora);
            if (mismoMedico && mismaFechaHora) {
                return true;
            }
        }
        return false;
    }

    /** Reprograma un turno existente a una nueva fecha/hora. */
    public void reprogramarTurno(Turno turno, LocalDate nuevaFecha, LocalTime nuevaHora) {
        if (!agenda.contains(turno)) {
            throw new IllegalArgumentException("El turno no pertenece a la agenda activa.");
        }
        if (haySolapamiento(turno.getMedico(), nuevaFecha, nuevaHora, turno)) {
            throw new IllegalStateException(
                "El médico ya tiene otro turno en " + nuevaFecha + " a las " + nuevaHora);
        }
        turno.setFecha(nuevaFecha);
        turno.setHora(nuevaHora);
        turnoDAO.guardarAgendaCompleta(agenda); // reescribe agenda.txt entero
    }

    /** Cancela un turno (sigue en agenda.txt, pero marcado como CANCELADO). */
    public void cancelarTurno(Turno turno) {
        if (!agenda.contains(turno)) {
            throw new IllegalArgumentException("El turno no pertenece a la agenda activa.");
        }
        turno.setEstado(EstadoTurno.CANCELADO);
        turnoDAO.guardarAgendaCompleta(agenda);
    }

    /**
     * Marca un turno como completado y lo mueve de la agenda activa
     * al historial (tanto en memoria como en los archivos de texto).
     */
    public void completarTurno(Turno turno) {
        if (!agenda.contains(turno)) {
            throw new IllegalArgumentException("El turno no pertenece a la agenda activa.");
        }
        turno.setEstado(EstadoTurno.COMPLETADO);
        turnoDAO.moverAHistorial(turno, agenda); // ya se encarga de reescribir agenda.txt
        agenda.remove(turno);
        historial.add(turno);
    }

    public List<Turno> obtenerAgenda() {
        return Collections.unmodifiableList(agenda);
    }

    public List<Turno> obtenerHistorial() {
        return Collections.unmodifiableList(historial);
    }

    // =========================================================
    //  OBSERVER (recordatorios)
    // =========================================================

    public void suscribir(AgendaObserver observador) {
        observadores.add(observador);
    }

    public void desuscribir(AgendaObserver observador) {
        observadores.remove(observador);
    }

    /**
     * Revisa la agenda buscando turnos que ocurran dentro de las
     * próximas "horasAntes" horas, y notifica a todos los
     * observadores suscriptos por cada uno que encuentra.
     *
     * Este método lo va a llamar RecordatorioThread cada cierto
     * tiempo (por ejemplo, cada 60 segundos) — GestorClinica no
     * sabe nada de threads, solo expone "che, avisame si hay algo
     * próximo", y el hilo decide cuándo preguntar.
     */
    public void verificarRecordatorios(int horasAntes) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusHours(horasAntes);

        for (Turno turno : agenda) {
            if (turno.getEstado() == EstadoTurno.CANCELADO) {
                continue;
            }
            LocalDateTime momentoTurno = LocalDateTime.of(turno.getFecha(), turno.getHora());
            boolean estaProximo = momentoTurno.isAfter(ahora) && momentoTurno.isBefore(limite);
            if (estaProximo) {
                notificarObservadores(turno);
            }
        }
    }

    private void notificarObservadores(Turno turno) {
        for (AgendaObserver observador : observadores) {
            observador.actualizar(turno);
        }
    }

    // =========================================================
    //  BAJAS Y MODIFICACIONES
    // =========================================================

    /**
    * Baja lógica: el paciente no se borra del mapa ni del archivo,
    * solo se marca activo = false. Así el historial de turnos que
    * tuvo sigue siendo consultable (si lo borráramos del todo,
    * los turnos viejos quedarían "huérfanos", sin paciente real
    * detrás del DNI guardado).
    */
    public boolean darDeBajaPaciente(int dni) {
        Paciente paciente = pacientes.get(dni);
        if (paciente == null) {
            throw new IllegalArgumentException("No existe un paciente con DNI " + dni);
        }
        paciente.setActivo(false);
        return pacienteDAO.actualizar(paciente);
    }

    public boolean darDeBajaMedico(int dni) {
        Medico medico = medicos.get(dni);
        if (medico == null) {
            throw new IllegalArgumentException("No existe un médico con DNI " + dni);
        }
        medico.setActivo(false);
        return medicoDAO.actualizar(medico);
    }

    /**
    * Modifica los datos editables de un paciente ya registrado.
    * El DNI no se puede cambiar (es la clave con la que se lo busca
    * en el mapa y en el archivo), por eso no aparece como parámetro
    * a modificar, solo como criterio de búsqueda.
    */
    public boolean modificarPaciente(int dni, String nombreNuevo, String apellidoNuevo,
                                  String telefonoNuevo, CoberturaMedica coberturaNueva) {
        Paciente paciente = pacientes.get(dni);
        if (paciente == null) {
            throw new IllegalArgumentException("No existe un paciente con DNI " + dni);
        }
        paciente.setNombre(nombreNuevo);
        paciente.setApellido(apellidoNuevo);
        paciente.setTelefono(telefonoNuevo);
        paciente.setCoberturaMedica(coberturaNueva);
        return pacienteDAO.actualizar(paciente);
    }

    public boolean modificarMedico(int dni, String nombreNuevo, String apellidoNuevo, String matriculaNueva) {
        Medico medico = medicos.get(dni);
        if (medico == null) {
            throw new IllegalArgumentException("No existe un médico con DNI " + dni);
        }
        medico.setNombre(nombreNuevo);
        medico.setApellido(apellidoNuevo);
        medico.setMatricula(matriculaNueva);
        return medicoDAO.actualizar(medico);
    }

}

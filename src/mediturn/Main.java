package mediturn;

import mediturn.model.*;
import mediturn.service.GestorClinica;
import mediturn.thread.NotificadorConsola;
import mediturn.thread.RecordatorioThread;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Main.java — Punto de entrada del sistema MediTurn.
 *
 * Esta clase es la ÚNICA responsable de la interacción con el usuario
 * por consola (mostrar menús, leer datos, mostrar resultados). Fiel a
 * la arquitectura en capas del proyecto (model -> util -> dao -> service
 * -> thread -> Main), Main nunca toca un DAO ni un archivo de texto
 * directamente: todo pasa por GestorClinica, el Singleton de la capa
 * "service" que conoce las reglas de negocio y coordina los DAO por
 * debajo. Esto es justamente lo que permite defender el patrón Singleton
 * en el oral: "hay un solo punto de entrada al sistema".
 */

public class Main {
   
    private static final Scanner scanner = new Scanner(System.in);

    // Formatos SOLO para lo que el usuario escribe en pantalla.
    // No tienen relación con el formato interno de agenda.txt/historial.txt
    // (eso lo maneja FormateadorFecha, en la capa util).
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    // Único acceso al sistema: el Singleton de la capa service.
    private static final GestorClinica gestor = GestorClinica.getInstancia();

    // El hilo de recordatorios no arranca solo: se crea/detiene desde el menú.
    private static RecordatorioThread hiloRecordatorios = null;

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("        Bienvenido a MediTurn");
        System.out.println("=====================================");

        boolean salir = false;
        while (!salir) {
            mostrarMenuPrincipal();
            int opcion = leerEntero("Elegí una opción: ");
            switch (opcion) {
                case 1:
                    menuPacientes();
                    break;
                case 2:
                    menuMedicos();
                    break;
                case 3:
                    menuTurnos();
                    break;
                case 4:
                    menuRecordatorios();
                    break;
                case 5:
                    exportarHistorial();
                    break;
                case 0:
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }

        // Si el hilo de recordatorios quedó corriendo, lo frenamos
        // prolijamente antes de cerrar el programa.
        if (hiloRecordatorios != null) {
            hiloRecordatorios.detener();
        }
        System.out.println("¡Hasta luego!");
        scanner.close();
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("\n----- Menú principal -----");
        System.out.println("1. Gestión de pacientes");
        System.out.println("2. Gestión de médicos");
        System.out.println("3. Gestión de turnos");
        System.out.println("4. Recordatorios automáticos");
        System.out.println("5. Exportar historial a archivo .txt");
        System.out.println("0. Salir");
    }

    // =========================================================
    //  PACIENTES
    // =========================================================

    private static void menuPacientes() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n----- Pacientes -----");
            System.out.println("1. Registrar paciente");
            System.out.println("2. Buscar paciente por DNI");
            System.out.println("3. Listar todos los pacientes");
            System.out.println("4. Modificar paciente");
            System.out.println("5. Dar de baja paciente");
            System.out.println("0. Volver");
            int opcion = leerEntero("Elegí una opción: ");

            switch (opcion) {
                case 1:
                    registrarPaciente();
                    break;
                case 2:
                    buscarPaciente();
                    break;
                case 3:
                    listarPacientes();
                    break;
                case 4:
                    modificarPaciente();
                    break;
                case 5:
                    darDeBajaPaciente();
                    break;
                case 0:
                    volver = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private static void registrarPaciente() {
        try {
            int dni = leerEntero("DNI: ");
            String nombre = leerTexto("Nombre: ");
            String apellido = leerTexto("Apellido: ");
            String telefono = leerTexto("Teléfono: ");
            CoberturaMedica cobertura = elegirCoberturaMedica();

            Paciente paciente = gestor.registrarPaciente(dni, nombre, apellido, telefono, cobertura);
            System.out.println("Paciente registrado: " + paciente.getNombre() + " " + paciente.getApellido());
        } catch (IllegalArgumentException e) {
            // Por ejemplo, DNI duplicado: GestorClinica valida esto
            // y tira la excepción; acá solo la mostramos al usuario.
            System.out.println("No se pudo registrar: " + e.getMessage());
        }
    }

    private static void buscarPaciente() {
        int dni = leerEntero("DNI a buscar: ");
        Paciente paciente = gestor.buscarPacientePorDni(dni);
        if (paciente == null) {
            System.out.println("No existe un paciente con ese DNI.");
        } else {
            System.out.println(paciente.mostrarInfo());
        }
    }

    private static void listarPacientes() {
        List<Paciente> pacientes = gestor.listarPacientes();
        if (pacientes.isEmpty()) {
            System.out.println("No hay pacientes registrados.");
            return;
        }
        for (Paciente p : pacientes) {
            System.out.println(p.mostrarInfo());
        }
    }

    private static void modificarPaciente() {
        int dni = leerEntero("DNI del paciente a modificar: ");
        Paciente actual = gestor.buscarPacientePorDni(dni);
        if (actual == null) {
            System.out.println("No existe un paciente con ese DNI.");
            return;
        }

        System.out.println("Datos actuales: " + actual.mostrarInfo());
        System.out.println("Ingresá los datos nuevos (dejar vacío mantiene el valor actual):");

        String nombre = leerTextoOpcional("Nombre [" + actual.getNombre() + "]: ", actual.getNombre());
        String apellido = leerTextoOpcional("Apellido [" + actual.getApellido() + "]: ", actual.getApellido());
        String telefono = leerTextoOpcional("Teléfono [" + actual.getTelefono() + "]: ", actual.getTelefono());

        CoberturaMedica cobertura = actual.getCoberturaMedica();
        System.out.print("¿Cambiar cobertura médica actual (" + cobertura.getNombreLegible() + ")? (s/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
            cobertura = elegirCoberturaMedica();
        }

        try {
            gestor.modificarPaciente(dni, nombre, apellido, telefono, cobertura);
            System.out.println("Paciente actualizado con éxito.");
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo modificar: " + e.getMessage());
        }
    }

    private static void darDeBajaPaciente() {
        int dni = leerEntero("DNI del paciente a dar de baja: ");
        Paciente paciente = gestor.buscarPacientePorDni(dni);
        if (paciente == null) {
            System.out.println("No existe un paciente con ese DNI.");
            return;
        }

        System.out.print("¿Confirmás dar de baja a " + paciente.getNombre() + " " + paciente.getApellido() + "? (s/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("s")) {
            System.out.println("Operación cancelada.");
            return;
        }

        try {
            // Baja LÓGICA, no física: el paciente queda marcado como
            // inactivo (activo = false) pero sigue en pacientes.txt.
            // Esto es necesario porque el historial y la agenda vieja
            // lo referencian por DNI: si lo borráramos del todo, esos
            // turnos quedarían "huérfanos" al reconstruirse desde archivo.
            gestor.darDeBajaPaciente(dni);
            System.out.println("Paciente dado de baja (queda inactivo, no se elimina su historial).");
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo dar de baja: " + e.getMessage());
        }
    }

    // =========================================================
    //  MÉDICOS
    // =========================================================

    private static void menuMedicos() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n----- Médicos -----");
            System.out.println("1. Registrar médico general");
            System.out.println("2. Registrar médico especialista");
            System.out.println("3. Buscar médico por DNI");
            System.out.println("4. Listar todos los médicos");
            System.out.println("5. Modificar médico");
            System.out.println("6. Dar de baja médico");
            System.out.println("0. Volver");
            int opcion = leerEntero("Elegí una opción: ");

            switch (opcion) {
                case 1:
                    registrarMedico();
                    break;
                case 2:
                    registrarMedicoEspecialista();
                    break;
                case 3:
                    buscarMedico();
                    break;
                case 4:
                    listarMedicos();
                    break;
                case 5:
                    modificarMedico();
                    break;
                case 6:
                    darDeBajaMedico();
                    break;
                case 0:
                    volver = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private static void registrarMedico() {
        try {
            int dni = leerEntero("DNI: ");
            String nombre = leerTexto("Nombre: ");
            String apellido = leerTexto("Apellido: ");
            String matricula = leerTexto("Matrícula: ");

            Medico medico = gestor.registrarMedico(dni, nombre, apellido, matricula);
            System.out.println("Médico registrado: " + medico.getNombre() + " " + medico.getApellido());
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo registrar: " + e.getMessage());
        }
    }

    private static void registrarMedicoEspecialista() {
        try {
            int dni = leerEntero("DNI: ");
            String nombre = leerTexto("Nombre: ");
            String apellido = leerTexto("Apellido: ");
            String matricula = leerTexto("Matrícula: ");
            Especialidad especialidad = elegirEspecialidad();

            MedicoEspecialista medico = gestor.registrarMedicoEspecialista(
                    dni, nombre, apellido, matricula, especialidad);
            System.out.println("Médico especialista registrado: "
                    + medico.getNombre() + " " + medico.getApellido()
                    + " (" + especialidad.getNombreLegible() + ")");
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo registrar: " + e.getMessage());
        }
    }

    private static void buscarMedico() {
        int dni = leerEntero("DNI a buscar: ");
        Medico medico = gestor.buscarMedicoPorDni(dni);
        if (medico == null) {
            System.out.println("No existe un médico con ese DNI.");
        } else {
            System.out.println(medico.mostrarInfo());
        }
    }

    private static void listarMedicos() {
        List<Medico> medicos = gestor.listarMedicos();
        if (medicos.isEmpty()) {
            System.out.println("No hay médicos registrados.");
            return;
        }
        for (Medico m : medicos) {
            System.out.println(m.mostrarInfo());
        }
    }

    private static void modificarMedico() {
        int dni = leerEntero("DNI del médico a modificar: ");
        Medico actual = gestor.buscarMedicoPorDni(dni);
        if (actual == null) {
            System.out.println("No existe un médico con ese DNI.");
            return;
        }

        System.out.println("Datos actuales: " + actual.mostrarInfo());
        System.out.println("Ingresá los datos nuevos (dejar vacío mantiene el valor actual):");

        String nombre = leerTextoOpcional("Nombre [" + actual.getNombre() + "]: ", actual.getNombre());
        String apellido = leerTextoOpcional("Apellido [" + actual.getApellido() + "]: ", actual.getApellido());
        String matricula = leerTextoOpcional("Matrícula [" + actual.getMatricula() + "]: ", actual.getMatricula());

        // Nota: modificarMedico no cambia la especialidad principal
        // (eso solo aplica a MedicoEspecialista y quedó fuera de este
        // método a propósito, para no complicar el ABM). Si hace falta
        // cambiar especialidad, se maneja aparte.
        try {
            gestor.modificarMedico(dni, nombre, apellido, matricula);
            System.out.println("Médico actualizado con éxito.");
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo modificar: " + e.getMessage());
        }
    }

    private static void darDeBajaMedico() {
        int dni = leerEntero("DNI del médico a dar de baja: ");
        Medico medico = gestor.buscarMedicoPorDni(dni);
        if (medico == null) {
            System.out.println("No existe un médico con ese DNI.");
            return;
        }

        System.out.print("¿Confirmás dar de baja a " + medico.getNombre() + " " + medico.getApellido() + "? (s/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("s")) {
            System.out.println("Operación cancelada.");
            return;
        }

        try {
            // Misma lógica que con pacientes: baja lógica (activo = false),
            // nunca se borra la fila. Un médico inactivo no debería poder
            // recibir turnos nuevos (esa validación vive en solicitarTurno,
            // dentro de GestorClinica), pero su historial de atenciones
            // pasadas se sigue pudiendo consultar sin problemas.
            gestor.darDeBajaMedico(dni);
            System.out.println("Médico dado de baja (queda inactivo, no se elimina su historial).");
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo dar de baja: " + e.getMessage());
        }
    }

    // =========================================================
    //  TURNOS
    // =========================================================

    private static void menuTurnos() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n----- Turnos -----");
            System.out.println("1. Solicitar turno");
            System.out.println("2. Reprogramar turno");
            System.out.println("3. Cancelar turno");
            System.out.println("4. Completar turno");
            System.out.println("5. Ver agenda activa");
            System.out.println("6. Ver historial completo");
            System.out.println("0. Volver");
            int opcion = leerEntero("Elegí una opción: ");

            switch (opcion) {
                case 1:
                    solicitarTurno();
                    break;
                case 2:
                    reprogramarTurno();
                    break;
                case 3:
                    cancelarTurno();
                    break;
                case 4:
                    completarTurno();
                    break;
                case 5:
                    verAgenda();
                    break;
                case 6:
                    verHistorial(gestor.obtenerHistorial());
                    break;
                case 0:
                    volver = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private static void solicitarTurno() {
        try {
            int dniPaciente = leerEntero("DNI del paciente: ");
            int dniMedico = leerEntero("DNI del médico: ");
            LocalDate fecha = leerFecha("Fecha (dd/mm/aaaa): ");
            LocalTime hora = leerHora("Hora (hh:mm): ");
            String tipoTurno = elegirTipoTurno();

            String detalleExtra;
            if (tipoTurno.equalsIgnoreCase("PRESENCIAL")) {
                detalleExtra = leerTexto("Consultorio: ");
            } else {
                detalleExtra = leerTexto("Link de videollamada: ");
            }

            Turno turno = gestor.solicitarTurno(dniPaciente, dniMedico, fecha, hora, tipoTurno, detalleExtra);
            System.out.println("Turno creado con éxito:");
            mostrarTurno(turno);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // IllegalArgumentException: paciente/médico inexistente.
            // IllegalStateException: solapamiento de horario.
            System.out.println("No se pudo solicitar el turno: " + e.getMessage());
        }
    }

    private static void reprogramarTurno() {
        Turno turno = elegirTurnoDeAgenda("¿Cuál turno querés reprogramar?");
        if (turno == null) return;

        try {
            LocalDate nuevaFecha = leerFecha("Nueva fecha (dd/mm/aaaa): ");
            LocalTime nuevaHora = leerHora("Nueva hora (hh:mm): ");
            gestor.reprogramarTurno(turno, nuevaFecha, nuevaHora);
            System.out.println("Turno reprogramado con éxito:");
            mostrarTurno(turno);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("No se pudo reprogramar: " + e.getMessage());
        }
    }

    private static void cancelarTurno() {
        Turno turno = elegirTurnoDeAgenda("¿Cuál turno querés cancelar?");
        if (turno == null) return;

        try {
            gestor.cancelarTurno(turno);
            System.out.println("Turno cancelado.");
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo cancelar: " + e.getMessage());
        }
    }

    private static void completarTurno() {
        Turno turno = elegirTurnoDeAgenda("¿Cuál turno querés marcar como completado?");
        if (turno == null) return;

        try {
            gestor.completarTurno(turno);
            System.out.println("Turno completado y movido al historial.");
        } catch (IllegalArgumentException e) {
            System.out.println("No se pudo completar: " + e.getMessage());
        }
    }

    private static void verAgenda() {
        List<Turno> agenda = gestor.obtenerAgenda();
        if (agenda.isEmpty()) {
            System.out.println("La agenda activa está vacía.");
            return;
        }
        System.out.println("\n--- Agenda activa (" + agenda.size() + " turnos) ---");
        for (Turno t : agenda) {
            mostrarTurno(t);
        }
    }

    private static void verHistorial(List<Turno> historial) {
        if (historial.isEmpty()) {
            System.out.println("El historial está vacío.");
            return;
        }
        System.out.println("\n--- Historial (" + historial.size() + " turnos) ---");
        for (Turno t : historial) {
            mostrarTurno(t);
        }
    }

    /**
     * Muestra la agenda activa numerada y devuelve el Turno elegido
     * por el usuario, o null si cancela o no hay turnos.
     * Centraliza la selección para no repetir este código en
     * reprogramar/cancelar/completar.
     */
    private static Turno elegirTurnoDeAgenda(String titulo) {
        List<Turno> agenda = gestor.obtenerAgenda();
        if (agenda.isEmpty()) {
            System.out.println("La agenda activa está vacía.");
            return null;
        }

        System.out.println("\n" + titulo);
        for (int i = 0; i < agenda.size(); i++) {
            System.out.print("[" + i + "] ");
            mostrarTurno(agenda.get(i));
        }
        System.out.println("[-1] Cancelar");

        int indice = leerEntero("Elegí un número: ");
        if (indice < 0 || indice >= agenda.size()) {
            return null;
        }
        return agenda.get(indice);
    }

    private static void mostrarTurno(Turno turno) {
        System.out.println("  Paciente: " + turno.getPaciente().getNombre() + " " + turno.getPaciente().getApellido()
                + " | Médico: " + turno.getMedico().getNombre() + " " + turno.getMedico().getApellido()
                + " | Fecha: " + turno.getFecha().format(FORMATO_FECHA)
                + " | Hora: " + turno.getHora().format(FORMATO_HORA)
                + " | Tipo: " + turno.getTipoTurno()
                + " | Detalle: " + turno.getDetalle()
                + " | Estado: " + turno.getEstado());
    }

    // =========================================================
    //  RECORDATORIOS (thread)
    // =========================================================

    private static void menuRecordatorios() {
        System.out.println("\n----- Recordatorios automáticos -----");
        if (hiloRecordatorios == null) {
            System.out.println("1. Iniciar hilo de recordatorios");
            System.out.println("0. Volver");
            int opcion = leerEntero("Elegí una opción: ");
            if (opcion == 1) {
                iniciarRecordatorios();
            }
        } else {
            System.out.println("El hilo de recordatorios ya está activo.");
            System.out.println("1. Detener hilo de recordatorios");
            System.out.println("0. Volver");
            int opcion = leerEntero("Elegí una opción: ");
            if (opcion == 1) {
                hiloRecordatorios.detener();
                hiloRecordatorios = null;
                System.out.println("Hilo detenido.");
            }
        }
    }

    private static void iniciarRecordatorios() {
        int horasAntes = leerEntero("¿Con cuántas horas de anticipación avisar?: ");
        int intervaloSegundos = leerEntero("¿Cada cuántos segundos revisar la agenda?: ");

        // NotificadorConsola es el Observer concreto (patrón Observer):
        // se suscribe a GestorClinica y decide qué hacer cuando hay un
        // turno próximo (en este caso, imprimir el aviso por consola).
        gestor.suscribir(new NotificadorConsola());

        hiloRecordatorios = new RecordatorioThread(intervaloSegundos * 1000L, horasAntes);
        hiloRecordatorios.start(); // dispara run() en un hilo aparte, no bloquea el menú
        System.out.println("Hilo de recordatorios iniciado.");
    }

    // =========================================================
    //  EXPORTAR HISTORIAL A .txt (reporte legible, no persistencia)
    // =========================================================

    private static void exportarHistorial() {
        System.out.println("\n----- Exportar historial -----");
        System.out.println("1. Historial completo");
        System.out.println("2. Historial de un paciente (por DNI)");
        System.out.println("3. Historial de un médico (por DNI)");
        System.out.println("0. Cancelar");
        int opcion = leerEntero("Elegí una opción: ");

        List<Turno> historialCompleto = gestor.obtenerHistorial();
        List<Turno> aExportar;
        String nombreArchivo;

        switch (opcion) {
            case 1:
                aExportar = historialCompleto;
                nombreArchivo = "reporte_historial_completo.txt";
                break;
            case 2: {
                int dni = leerEntero("DNI del paciente: ");
                aExportar = historialCompleto.stream()
                        .filter(t -> t.getPaciente().getDni() == dni)
                        .toList();
                nombreArchivo = "reporte_historial_paciente_" + dni + ".txt";
                break;
            }
            case 3: {
                int dni = leerEntero("DNI del médico: ");
                aExportar = historialCompleto.stream()
                        .filter(t -> t.getMedico().getDni() == dni)
                        .toList();
                nombreArchivo = "reporte_historial_medico_" + dni + ".txt";
                break;
            }
            default:
                System.out.println("Cancelado.");
                return;
        }

        if (aExportar.isEmpty()) {
            System.out.println("No hay turnos para exportar con ese criterio.");
            return;
        }

        // Este archivo es un REPORTE pensado para que lo lea una persona,
        // distinto de historial.txt (que es la persistencia real del
        // sistema y la maneja TurnoDAO con su propio formato CSV).
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo))) {
            writer.write("Reporte de historial - MediTurn");
            writer.newLine();
            writer.newLine();
            for (Turno t : aExportar) {
                writer.write("Paciente: " + t.getPaciente().getNombre() + " " + t.getPaciente().getApellido());
                writer.newLine();
                writer.write("Médico: " + t.getMedico().getNombre() + " " + t.getMedico().getApellido());
                writer.newLine();
                writer.write("Fecha: " + t.getFecha().format(FORMATO_FECHA) + " - Hora: " + t.getHora().format(FORMATO_HORA));
                writer.newLine();
                writer.write("Tipo: " + t.getTipoTurno() + " - Detalle: " + t.getDetalle());
                writer.newLine();
                writer.write("Estado: " + t.getEstado());
                writer.newLine();
                writer.write("-------------------------------------");
                writer.newLine();
            }
            System.out.println("Reporte exportado a " + nombreArchivo);
        } catch (IOException e) {
            System.out.println("Error al exportar: " + e.getMessage());
        }
    }

    // =========================================================
    //  HELPERS DE LECTURA POR CONSOLA
    // =========================================================

    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String linea = scanner.nextLine().trim();
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.println("Ingresá un número válido.");
            }
        }
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    /**
     * Igual que leerTexto(), pero si el usuario deja la línea vacía
     * (solo Enter), devuelve valorActual en vez de un String vacío.
     * Se usa en los formularios de "modificar", para no obligar a
     * reescribir todos los campos si solo querés cambiar uno.
     */
    private static String leerTextoOpcional(String mensaje, String valorActual) {
        System.out.print(mensaje);
        String linea = scanner.nextLine().trim();
        return linea.isEmpty() ? valorActual : linea;
    }

    private static LocalDate leerFecha(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String linea = scanner.nextLine().trim();
            try {
                return LocalDate.parse(linea, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido. Usá dd/mm/aaaa (ej: 25/12/2026).");
            }
        }
    }

    private static LocalTime leerHora(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String linea = scanner.nextLine().trim();
            try {
                return LocalTime.parse(linea, FORMATO_HORA);
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido. Usá hh:mm (ej: 14:30).");
            }
        }
    }

    /** Muestra el enum CoberturaMedica como menú numerado y devuelve la opción elegida. */
    private static CoberturaMedica elegirCoberturaMedica() {
        CoberturaMedica[] valores = CoberturaMedica.values();
        System.out.println("Cobertura médica:");
        for (int i = 0; i < valores.length; i++) {
            System.out.println((i + 1) + ". " + valores[i].getNombreLegible());
        }
        int opcion = leerEntero("Elegí una opción: ");
        while (opcion < 1 || opcion > valores.length) {
            System.out.println("Opción inválida.");
            opcion = leerEntero("Elegí una opción: ");
        }
        return valores[opcion - 1];
    }

    /** Muestra el enum Especialidad como menú numerado y devuelve la opción elegida. */
    private static Especialidad elegirEspecialidad() {
        Especialidad[] valores = Especialidad.values();
        System.out.println("Especialidad:");
        for (int i = 0; i < valores.length; i++) {
            System.out.println((i + 1) + ". " + valores[i].getNombreLegible());
        }
        int opcion = leerEntero("Elegí una opción: ");
        while (opcion < 1 || opcion > valores.length) {
            System.out.println("Opción inválida.");
            opcion = leerEntero("Elegí una opción: ");
        }
        return valores[opcion - 1];
    }

    /** Pregunta PRESENCIAL o VIRTUAL (los valores que espera TurnoFactory). */
    private static String elegirTipoTurno() {
        System.out.println("Tipo de turno:");
        System.out.println("1. Presencial");
        System.out.println("2. Virtual");
        int opcion = leerEntero("Elegí una opción: ");
        while (opcion != 1 && opcion != 2) {
            System.out.println("Opción inválida.");
            opcion = leerEntero("Elegí una opción: ");
        }
        return (opcion == 1) ? "PRESENCIAL" : "VIRTUAL";
    }

}

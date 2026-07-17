package mediturn.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mediturn.model.EstadoTurno;
import mediturn.model.Medico;
import mediturn.model.Paciente;
import mediturn.model.Turno;
import mediturn.model.TurnoPresencial;
import mediturn.model.TurnoVirtual;
import mediturn.util.Constantes;
import mediturn.util.CsvUtil;
import mediturn.util.FormateadorFecha;
import mediturn.util.ValidacionUtil;

public class TurnoDAO {
    
    // fecha;hora;dniPaciente;dniMedico;estado;tipo;detalle
    private static final int CANTIDAD_CAMPOS = 7;
    private static final String TIPO_PRESENCIAL = "Presencial";
    private static final String TIPO_VIRTUAL = "Virtual";

    /** Agrega un turno nuevo al final de agenda.txt (turno activo, recién creado). */
    public void guardarEnAgenda(Turno turno) {
        escribirLinea(Constantes.ARCHIVO_AGENDA, turno.toLineaArchivo(), true);
    }

    /** Lee todos los turnos activos de agenda.txt, ya reconstruidos. */
    public List<Turno> listarAgenda(Map<Integer, Paciente> pacientes, Map<Integer, Medico> medicos) {
        return leerArchivo(Constantes.ARCHIVO_AGENDA, pacientes, medicos);
    }

    /** Lee todos los turnos completados de historial.txt. */
    public List<Turno> listarHistorial(Map<Integer, Paciente> pacientes, Map<Integer, Medico> medicos) {
        return leerArchivo(Constantes.ARCHIVO_HISTORIAL, pacientes, medicos);
    }

    /**
     * Reescribe agenda.txt completo. Se usa después de cancelar,
     * reprogramar, o sacar un turno (porque se completó).
     */
    public void guardarAgendaCompleta(List<Turno> turnosActivos) {
        reescribirArchivo(Constantes.ARCHIVO_AGENDA, turnosActivos);
    }

    /**
     * Mueve un turno de la agenda activa al historial: lo agrega a
     * historial.txt y reescribe agenda.txt sin él. Se llama cuando
     * el service marca un turno como COMPLETADO.
     */
    public void moverAHistorial(Turno turno, List<Turno> agendaActual) {
        escribirLinea(Constantes.ARCHIVO_HISTORIAL, turno.toLineaArchivo(), true);
        agendaActual.remove(turno);
        guardarAgendaCompleta(agendaActual);
    }

    // ---------- privados ----------

    private void escribirLinea(String archivo, String linea, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo, append))) {
            writer.write(linea);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error al escribir en " + archivo + ": " + e.getMessage());
        }
    }

    private void reescribirArchivo(String archivo, List<Turno> turnos) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo, false))) {
            for (Turno t : turnos) {
                writer.write(t.toLineaArchivo());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al reescribir " + archivo + ": " + e.getMessage());
        }
    }

    private List<Turno> leerArchivo(String archivo, Map<Integer, Paciente> pacientes, Map<Integer, Medico> medicos) {
        List<Turno> turnos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                Turno turno = parsearLinea(linea, pacientes, medicos);
                if (turno != null) {
                    turnos.add(turno);
                }
            }
        } catch (IOException e) {
            System.out.println("Aún no existe " + archivo + " (se creará al guardar el primer turno).");
        }
        return turnos;
    }

    private Turno parsearLinea(String linea, Map<Integer, Paciente> pacientes, Map<Integer, Medico> medicos) {
        if (ValidacionUtil.estaVacio(linea)) {
            return null;
        }

        String[] campos = CsvUtil.dividirLinea(linea);
        if (!ValidacionUtil.tieneCantidadDeCampos(campos, CANTIDAD_CAMPOS)) {
            System.out.println("Línea inválida en archivo de turnos (se ignora): " + linea);
            return null;
        }

        try {
            LocalDate fecha = FormateadorFecha.parsearFecha(campos[0]);
            LocalTime hora = FormateadorFecha.parsearHora(campos[1]);
            int dniPaciente = Integer.parseInt(campos[2]);
            int dniMedico = Integer.parseInt(campos[3]);
            EstadoTurno estado = EstadoTurno.valueOf(campos[4]);
            String tipo = campos[5];
            String detalle = campos[6];

            Paciente paciente = pacientes.get(dniPaciente);
            Medico medico = medicos.get(dniMedico);

            // Si el paciente/médico no está cargado, o la fecha/hora
            // no se pudo parsear, el turno queda incompleto: mejor
            // descartarlo que crear un objeto con datos en null.
            if (paciente == null || medico == null || fecha == null || hora == null) {
                System.out.println("Turno con datos incompletos (se ignora): " + linea);
                return null;
            }

            Turno turno;
            if (TIPO_PRESENCIAL.equals(tipo)) {
                turno = new TurnoPresencial(fecha, hora, paciente, medico, quitarPrefijo(detalle, "Consultorio:"));
            } else if (TIPO_VIRTUAL.equals(tipo)) {
                turno = new TurnoVirtual(fecha, hora, paciente, medico, quitarPrefijo(detalle, "Link:"));
            } else {
                System.out.println("Tipo de turno desconocido (se ignora): " + linea);
                return null;
            }

            turno.setEstado(estado);
            return turno;
        } catch (IllegalArgumentException e) {
            System.out.println("Línea inválida en archivo de turnos (se ignora): " + linea);
            return null;
        }
    }

    private String quitarPrefijo(String texto, String prefijo) {
        return texto.startsWith(prefijo) ? texto.substring(prefijo.length()) : texto;
    }


}

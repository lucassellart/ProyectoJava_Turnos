package mediturn.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mediturn.model.CoberturaMedica;
import mediturn.model.Paciente;
import mediturn.util.Constantes;
import mediturn.util.CsvUtil;
import mediturn.util.ValidacionUtil;

/**
 * DAO (Data Access Object) de Paciente.
 *
 * La idea del patrón DAO es esta: esta clase es la ÚNICA que sabe
 * "cómo" se guarda un Paciente en disco (formato de línea, manejo
 * de BufferedReader/Writer, etc). El resto del sistema (service, Main)
 * solo le pide "guardame esto" o "dame todos los pacientes", sin
 * saber que por detrás hay un archivo de texto. Si mañana cambiás
 * el formato de guardado, solo tocás esta clase.
 */

public class PacienteDAO {
   
    // dni;nombre;apellido;telefono;coberturaMedica
    private static final int CANTIDAD_CAMPOS = 6;

    /**
     * Agrega un paciente nuevo al final de pacientes.txt.
     * El "true" en FileWriter indica modo "append": no borra lo que
     * ya había, solo agrega la línea nueva al final.
     */
    public void guardar(Paciente paciente) {
        String linea = construirLinea(paciente);

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(Constantes.ARCHIVO_PACIENTES, true))) {
            writer.write(linea);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error al guardar el paciente: " + e.getMessage());
        }
    }

    /**
     * Lee pacientes.txt entero y devuelve la lista reconstruida.
     * Si el archivo todavía no existe (primera vez que corre el
     * programa), devolvemos lista vacía en vez de romper.
     */
    public List<Paciente> listarTodos() {
        List<Paciente> pacientes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(Constantes.ARCHIVO_PACIENTES))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                Paciente paciente = parsearLinea(linea);
                if (paciente != null) {
                    pacientes.add(paciente);
                }
            }
        } catch (IOException e) {
            System.out.println("Aún no existe " + Constantes.ARCHIVO_PACIENTES
                    + " (se creará al guardar el primer paciente).");
        }

        return pacientes;
    }

    /**
    * Actualiza los datos de un paciente ya existente (identificado por DNI)
    * y también sirve para dar de "baja lógica": si el objeto que llega
    * tiene activo = false, eso es lo que va a quedar guardado.
    * Reescribe el archivo completo, igual que guardarAgendaCompleta() en TurnoDAO,
    * porque no se puede editar una sola línea de un archivo de texto sin
    * reescribir el resto.
    *
    * @return true si encontró y actualizó al paciente, false si no existía ese DNI.
    */
    
    public boolean actualizar(Paciente pacienteActualizado) {
        List<Paciente> pacientes = listarTodos();
        boolean encontrado = false;

        for (int i = 0; i < pacientes.size(); i++) {
            if (pacientes.get(i).getDni() == pacienteActualizado.getDni()) {
                pacientes.set(i, pacienteActualizado);
                encontrado = true;
                break;
            }
        }

        if (encontrado) {
            guardarTodos(pacientes);
        }
        return encontrado;
    }

    /**
     * Igual que listarTodos(), pero devuelve un Map<dni, Paciente>.
     * Lo va a usar TurnoDAO para buscar un paciente por DNI en O(1)
     * en vez de recorrer una lista entera con un for cada vez.
     */
    public Map<Integer, Paciente> listarComoMapa() {
        Map<Integer, Paciente> mapa = new HashMap<>();
        for (Paciente p : listarTodos()) {
            mapa.put(p.getDni(), p);
        }
        return mapa;
    }

    /**
     * Reescribe pacientes.txt completo a partir de una lista.
     * A diferencia de guardar() (que solo agrega una línea), este
     * método se usa cuando hay que dar de baja o modificar un
     * paciente: no se puede "borrar una línea" de un archivo de
     * texto directamente, así que se reescribe todo sin la línea vieja.
     */
    public void guardarTodos(List<Paciente> pacientes) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(Constantes.ARCHIVO_PACIENTES, false))) {
            for (Paciente p : pacientes) {
                writer.write(construirLinea(p));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al reescribir pacientes.txt: " + e.getMessage());
        }
    }

    private String construirLinea(Paciente paciente) {
        return CsvUtil.unirCampos(
                String.valueOf(paciente.getDni()),
                paciente.getNombre(),
                paciente.getApellido(),
                paciente.getTelefono(),
                paciente.getCoberturaMedica().name(),
                String.valueOf(paciente.isActivo())
        );
    }

    /**
     * Convierte una línea de texto en un objeto Paciente.
     * Devuelve null ante una línea corrupta, en vez de tirar una
     * excepción que cortaría la carga de todo el archivo por un
     * solo error.
     */
    private Paciente parsearLinea(String linea) {
        if (ValidacionUtil.estaVacio(linea)) {
            return null;
        }

        String[] campos = CsvUtil.dividirLinea(linea);
        if (!ValidacionUtil.tieneCantidadDeCampos(campos, CANTIDAD_CAMPOS)) {
            System.out.println("Línea inválida en pacientes.txt (se ignora): " + linea);
            return null;
        }

        try {
            int dni = Integer.parseInt(campos[0]);
            String nombre = campos[1];
            String apellido = campos[2];
            String telefono = campos[3];
            CoberturaMedica cobertura = CoberturaMedica.valueOf(campos[4]);
            boolean activo = Boolean.parseBoolean(campos[5]);

            Paciente paciente = new Paciente(dni, nombre, apellido, cobertura, telefono);
            paciente.setActivo(activo);
            return paciente;
        } catch (IllegalArgumentException e) {
            System.out.println("Línea inválida en pacientes.txt (se ignora): " + linea);
            return null;
        }
    }

}

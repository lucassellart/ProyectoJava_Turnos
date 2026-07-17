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

import mediturn.model.Especialidad;
import mediturn.model.Medico;
import mediturn.model.MedicoEspecialista;
import mediturn.util.Constantes;
import mediturn.util.CsvUtil;
import mediturn.util.ValidacionUtil;

/**
 * DAO de Medico. Mismo patrón que PacienteDAO, pero con una
 * complicación extra: Medico tiene una subclase (MedicoEspecialista)
 * con un campo de más (especialidadPrincipal). Usamos "instanceof"
 * para detectar de qué tipo es cada médico al guardar, y un campo
 * "tipo" al principio de cada línea para saber cómo reconstruirlo
 * al leer.
 */

public class MedicoDAO {
    
    private static final String TIPO_GENERAL = "GENERAL";
    private static final String TIPO_ESPECIALISTA = "ESPECIALISTA";
    private static final String SEPARADOR_ESPECIALIDADES = ",";

    // tipo;dni;nombre;apellido;matricula;especialidades;especialidadPrincipal
    private static final int CANTIDAD_CAMPOS = 8;

    public void guardar(Medico medico) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(Constantes.ARCHIVO_MEDICOS, true))) {
            writer.write(construirLinea(medico));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error al guardar el médico: " + e.getMessage());
        }
    }

    public List<Medico> listarTodos() {
        List<Medico> medicos = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(Constantes.ARCHIVO_MEDICOS))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                Medico medico = parsearLinea(linea);
                if (medico != null) {
                    medicos.add(medico);
                }
            }
        } catch (IOException e) {
            System.out.println("Aún no existe " + Constantes.ARCHIVO_MEDICOS
                    + " (se creará al guardar el primer médico).");
        }

        return medicos;
    }

    public Map<Integer, Medico> listarComoMapa() {
        Map<Integer, Medico> mapa = new HashMap<>();
        for (Medico m : listarTodos()) {
            mapa.put(m.getDni(), m);
        }
        return mapa;
    }

    public void guardarTodos(List<Medico> medicos) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(Constantes.ARCHIVO_MEDICOS, false))) {
            for (Medico m : medicos) {
                writer.write(construirLinea(m));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al reescribir medicos.txt: " + e.getMessage());
        }
    }

    /**
     * Arma la línea según el tipo real del objeto.
     * Nota para tu defensa: esto es un "instanceof" clásico, que
     * normalmente se trata de evitar en diseño OOP (rompe un poco
     * el polimorfismo). Acá se justifica porque no es lógica de
     * negocio, es solo el detalle de cuántos campos escribir en el
     * archivo — una responsabilidad exclusiva de esta capa dao.
     */
    private String construirLinea(Medico medico) {
        String especialidades = unirEspecialidades(medico.getEspecialidades());

        if (medico instanceof MedicoEspecialista especialista) {
            return CsvUtil.unirCampos(
                    TIPO_ESPECIALISTA,
                    String.valueOf(especialista.getDni()),
                    especialista.getNombre(),
                    especialista.getApellido(),
                    especialista.getMatricula(),
                    especialidades,
                    especialista.getEspecialidadPrincipal().name(),
                    String.valueOf(especialista.isActivo())
            );
        }

        return CsvUtil.unirCampos(
                TIPO_GENERAL,
                String.valueOf(medico.getDni()),
                medico.getNombre(),
                medico.getApellido(),
                medico.getMatricula(),
                especialidades,
                "",
                String.valueOf(medico.isActivo())
        );
    }

    private String unirEspecialidades(List<Especialidad> especialidades) {
        if (especialidades == null || especialidades.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < especialidades.size(); i++) {
            sb.append(especialidades.get(i).name());
            if (i < especialidades.size() - 1) {
                sb.append(SEPARADOR_ESPECIALIDADES);
            }
        }
        return sb.toString();
    }

    private Medico parsearLinea(String linea) {
    if (ValidacionUtil.estaVacio(linea)) {
        return null;
    }

    String[] campos = CsvUtil.dividirLinea(linea);
    if (!ValidacionUtil.tieneCantidadDeCampos(campos, CANTIDAD_CAMPOS)) {
        System.out.println("Línea inválida en medicos.txt (se ignora): " + linea);
        return null;
    }

    try {
        String tipo = campos[0];
        int dni = Integer.parseInt(campos[1]);
        String nombre = campos[2];
        String apellido = campos[3];
        String matricula = campos[4];
        List<Especialidad> especialidades = parsearEspecialidades(campos[5]);
        boolean activo = Boolean.parseBoolean(campos[7]);

        Medico medico;
        if (TIPO_ESPECIALISTA.equals(tipo)) {
            Especialidad principal = Especialidad.valueOf(campos[6]);
            medico = new MedicoEspecialista(dni, nombre, apellido, matricula, principal);
        } else {
            medico = new Medico(dni, nombre, apellido, matricula);
        }

        for (Especialidad e : especialidades) {
            medico.agregarEspecialidad(e);
        }
        medico.setActivo(activo);

        return medico;
    } catch (IllegalArgumentException e) {
        System.out.println("Línea inválida en medicos.txt (se ignora): " + linea);
        return null;
    }
}

    private List<Especialidad> parsearEspecialidades(String campo) {
        List<Especialidad> lista = new ArrayList<>();
        if (ValidacionUtil.estaVacio(campo)) {
            return lista;
        }
        for (String parte : campo.split(SEPARADOR_ESPECIALIDADES)) {
            lista.add(Especialidad.valueOf(parte));
        }
        return lista;
    }



public boolean actualizar(Medico medicoActualizado) {
    List<Medico> medicos = listarTodos();
    boolean encontrado = false;

    for (int i = 0; i < medicos.size(); i++) {
        if (medicos.get(i).getDni() == medicoActualizado.getDni()) {
            medicos.set(i, medicoActualizado);
            encontrado = true;
            break;
        }
    }

    if (encontrado) {
        guardarTodos(medicos);
    }
    return encontrado;
}

}

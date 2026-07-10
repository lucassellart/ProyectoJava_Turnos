package mediturn.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa a un médico de la clínica.
 * Hereda dni, nombre y apellido de Persona, y le agrega lo propio.
 */

public class Medico extends Persona {
    
    private String matricula;

    // PLACEHOLDER: cuando definamos la clase Especialidad,
    // esto pasa a ser List<Especialidad>.
    private List<String> especialidades;

    public Medico(int dni, String nombre, String apellido, String matricula) {
        // super(...) llama al constructor de Persona. Es obligatorio
        // que sea la primera línea si Persona no tiene un constructor
        // vacío (y en este caso no lo tiene).
        super(dni, nombre, apellido);
        this.matricula = matricula;
        this.especialidades = new ArrayList<>();
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public List<String> getEspecialidades() {
        return especialidades;
    }

    public void agregarEspecialidad(String especialidad) {
        especialidades.add(especialidad);
    }

    /**
     * Acá implementamos el método abstracto de Persona.
     * @Override le indica al compilador "esto tiene que
     * corresponder a un método de la clase padre", y si te
     * equivocás en el nombre o los parámetros, te tira error
     * en vez de crear un método nuevo por accidente.
     */
    @Override
    public String mostrarInfo() {
        return "Médico - " + super.toString()
                + " - Matrícula: " + matricula
                + " - Especialidades: " + especialidades;
    }
}


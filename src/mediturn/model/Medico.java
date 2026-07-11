package mediturn.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa a un médico de la clínica.
 * Hereda dni, nombre y apellido de Persona, y le agrega lo propio.
 */

public class Medico extends Persona {
    
    private String matricula;

    private List<Especialidad> especialidades;

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

    public List<Especialidad> getEspecialidades() {
        return especialidades;
    }

    public void agregarEspecialidad(Especialidad especialidad) {
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

    /**
    * Calcula el costo de la consulta para un médico general.
    * Este método existe en la clase base para que las subclases
    * (como MedicoEspecialista) puedan sobreescribirlo con @Override
    * y así aplicar polimorfismo: cada tipo de médico calcula su costo
    * de forma distinta, pero se invoca de la misma manera.
    */
    public double calcularCosto() {
        return 5000.0; // valor base de consulta general, ajustá según tu caso
    }
}


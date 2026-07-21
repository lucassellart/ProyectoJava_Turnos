package mediturn.model;

import java.time.LocalDate;

/**
 * Representa a un paciente de la clínica.
 */
public class Paciente extends Persona {
    private CoberturaMedica coberturaMedica;
    private String telefono;
    private LocalDate fechaNacimiento;

    public Paciente(int dni, String nombre, String apellido,
                     CoberturaMedica coberturaMedica, String telefono,
                     LocalDate fechaNacimiento) {
        super(dni, nombre, apellido);
        this.coberturaMedica = coberturaMedica;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Valida la "contraseña" del login del paciente, que en este
     * sistema (igual que en Logimed) es el día y el mes de su fecha
     * de nacimiento, no una clave que el paciente elige. No guardamos
     * una contraseña aparte: se calcula a partir de un dato que el
     * paciente ya tiene cargado, así evitamos duplicar información.
     */
    public boolean validarContrasena(int dia, int mes) {
        return fechaNacimiento != null
                && fechaNacimiento.getDayOfMonth() == dia
                && fechaNacimiento.getMonthValue() == mes;
    }

    public CoberturaMedica getCoberturaMedica() {
        return coberturaMedica;
    }

    public void setCoberturaMedica(CoberturaMedica coberturaMedica) {
        this.coberturaMedica = coberturaMedica;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String mostrarInfo() {
        
        String estado = activo ? "" : " (inactivo)";
        
        return "Paciente - " + super.toString()
            + " - Cobertura: " + coberturaMedica.getNombreLegible()
            + " - Tel: " + telefono
            + estado;
    }
}

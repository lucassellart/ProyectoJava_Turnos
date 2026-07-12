package mediturn.model;

/**
 * Representa a un paciente de la clínica.
 */
public class Paciente extends Persona {
    private CoberturaMedica coberturaMedica;
    private String telefono;

    public Paciente(int dni, String nombre, String apellido,
                     CoberturaMedica coberturaMedica, String telefono) {
        super(dni, nombre, apellido);
        this.coberturaMedica = coberturaMedica;
        this.telefono = telefono;
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
        return "Paciente - " + super.toString()
                + " - Cobertura: " + coberturaMedica.getNombreLegible()
                + " - Tel: " + telefono;
    }
}

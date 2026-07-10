package mediturn.model;

/**
 * Representa a un paciente de la clínica.
 */

public class Paciente extends Persona {
    
    private String obraSocial;
    private String telefono;

    public Paciente(int dni, String nombre, String apellido,
                     String obraSocial, String telefono) {
        super(dni, nombre, apellido);
        this.obraSocial = obraSocial;
        this.telefono = telefono;
    }

    public String getObraSocial() {
        return obraSocial;
    }

    public void setObraSocial(String obraSocial) {
        this.obraSocial = obraSocial;
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
                + " - Obra social: " + obraSocial
                + " - Tel: " + telefono;
    }

}

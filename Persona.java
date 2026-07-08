package mediturn.model;

import java.util.Objects;

public abstract class Persona {
    
    protected int dni;
    protected String nombre;
    protected String apellido;

    public Persona(int d_dni, String n_nombre, String a_apellido) {
        this.dni = d_dni;
        this.nombre = n_nombre;
        this.apellido = a_apellido;
    }

    public int getDni() {
        return dni;
    }

    public void setDni(int dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public abstract String mostrarInfo();

    public String toString() {
        return "DNI: " + dni + " - " + nombre + " " + apellido;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Persona otra = (Persona) obj;
        return dni == otra.dni;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dni);
    }

}

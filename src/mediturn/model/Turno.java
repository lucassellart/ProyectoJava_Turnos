package mediturn.model;

import java.time.LocalDate;
import java.time.LocalTime;

public abstract class Turno {
    
    // LocalDate y LocalTime son clases de Java para manejar fechas y horas
    // de forma segura (evitan errores comunes de manejar todo como String).
    protected LocalDate fecha;
    protected LocalTime hora;
    protected Paciente paciente;
    protected Medico medico;
    protected EstadoTurno estado;

    public Turno(LocalDate fecha, LocalTime hora, Paciente paciente, Medico medico) {
        this.fecha = fecha;
        this.hora = hora;
        this.paciente = paciente;
        this.medico = medico;
        // Todo turno nuevo arranca en estado PENDIENTE por defecto
        this.estado = EstadoTurno.PENDIENTE;
    }

    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public Paciente getPaciente() { return paciente; }
    public Medico getMedico() { return medico; }
    public EstadoTurno getEstado() { return estado; }
    public void setEstado(EstadoTurno estado) { this.estado = estado; }

    /**
     * Método abstracto: cada subclase (Presencial/Virtual) debe definir
     * cómo se muestra su información particular. Esto es polimorfismo:
     * el mismo método se comporta distinto según el tipo real del objeto.
     */
    public abstract String getDetalle();

    /**
     * Método pensado para el formato de guardado en agenda.txt / historial.txt.
     * Lo dejamos definido acá porque la lógica de "qué campos comunes van
     * primero" es igual para ambos tipos; el detalle específico se arma
     * llamando a getDetalle() de la subclase correspondiente.
     */
    public String toLineaArchivo() {
        return fecha + ";" + hora + ";" + paciente.getDni() + ";" 
             + medico.getDni() + ";" + estado + ";" + getDetalle();
    }

}

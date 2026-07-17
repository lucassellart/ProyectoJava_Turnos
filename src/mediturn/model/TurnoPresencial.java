package mediturn.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class TurnoPresencial extends Turno {
    
    private String consultorio;

    public TurnoPresencial(LocalDate fecha, LocalTime hora, Paciente paciente,
                            Medico medico, String consultorio) {
        super(fecha, hora, paciente, medico);
        this.consultorio = consultorio;
    }

    public String getConsultorio() { return consultorio; }
    public void setConsultorio(String consultorio) { this.consultorio = consultorio; }

    @Override
    public String getTipoTurno() {
        return "Presencial";
    }

    @Override
    public String getDetalle() {
        return "Consultorio:" + consultorio;
    }

}

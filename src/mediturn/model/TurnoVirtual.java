package mediturn.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class TurnoVirtual extends Turno {
    
    private String linkVideollamada;

    public TurnoVirtual(LocalDate fecha, LocalTime hora, Paciente paciente,
                         Medico medico, String linkVideollamada) {
        super(fecha, hora, paciente, medico);
        this.linkVideollamada = linkVideollamada;
    }

    public String getLinkVideollamada() { return linkVideollamada; }
    public void setLinkVideollamada(String linkVideollamada) { this.linkVideollamada = linkVideollamada; }

    @Override
    public String getDetalle() {
        return "Virtual;Link:" + linkVideollamada;
    }

}

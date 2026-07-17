package mediturn.thread;

import mediturn.model.Turno;
import mediturn.service.AgendaObserver;

public class NotificadorConsola implements AgendaObserver {
    
    @Override
    public void actualizar(Turno turno) {
        System.out.println("=====================================");
        System.out.println("RECORDATORIO: turno próximo");
        System.out.println("Paciente : " + turno.getPaciente().getNombre());
        System.out.println("Médico   : " + turno.getMedico().getNombre());
        System.out.println("Fecha    : " + turno.getFecha());
        System.out.println("Hora     : " + turno.getHora());
        System.out.println("Tipo     : " + turno.getTipoTurno());
        System.out.println("Detalle  : " + turno.getDetalle());
        System.out.println("=====================================");
    }
}

package mediturn.model;

/**
 * Representa a un médico especializado en un área puntual (más allá
 * de la clínica general). Hereda de Medico y sobreescribe calcularCosto()
 * para reflejar que las consultas con especialistas suelen tener un
 * costo mayor que las consultas generales.
 */

public class MedicoEspecialista extends Medico{
    
    // Recargo aplicado sobre el costo base de una consulta general.
    // Se podría ajustar por especialidad en el futuro, pero por ahora
    // es un valor fijo para mantener la lógica simple.
    private static final double RECARGO_ESPECIALISTA = 3000.0;

    private Especialidad especialidadPrincipal;

    public MedicoEspecialista(int dni, String nombre, String apellido,
                              String matricula, Especialidad especialidadPrincipal) {
        // Llama al constructor de Medico para inicializar los campos heredados
        super(dni, nombre, apellido, matricula);
        this.especialidadPrincipal = especialidadPrincipal;
    }

    public Especialidad getEspecialidadPrincipal() {
        return especialidadPrincipal;
    }

    public void setEspecialidadPrincipal(Especialidad especialidadPrincipal) {
        this.especialidadPrincipal = especialidadPrincipal;
    }

    /**
     * Sobreescribe el cálculo de costo del médico general, sumando
     * un recargo por ser consulta con especialista.
     * @Override es obligatorio ponerlo: le indica al compilador que
     * esta firma debe coincidir exactamente con la de la clase padre,
     * y si no coincide, te avisa con un error en vez de crear un
     * método nuevo por accidente (un error muy común).
     */
    @Override
    public double calcularCosto() {
        return super.calcularCosto() + RECARGO_ESPECIALISTA;
    }

}

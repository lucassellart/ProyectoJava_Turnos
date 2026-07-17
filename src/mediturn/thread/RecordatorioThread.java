package mediturn.thread;

import mediturn.service.GestorClinica;

public class RecordatorioThread extends Thread {
    
    private final long intervaloMs;
    private final int horasAntes;
    private volatile boolean activo = true;

    public RecordatorioThread(long intervaloMs, int horasAntes) {
        this.intervaloMs = intervaloMs;
        this.horasAntes = horasAntes;
        setDaemon(true);
    }

    @Override
    public void run() {
        System.out.println("[RecordatorioThread] Iniciado. Revisando cada "
                + (intervaloMs / 1000) + "s, con " + horasAntes + "hs de anticipación.");

        while (activo) {
            try {
                GestorClinica.getInstancia().verificarRecordatorios(horasAntes);
                Thread.sleep(intervaloMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                activo = false;
            }
        }

        System.out.println("[RecordatorioThread] Detenido.");
    }

    public void detener() {
        activo = false;
        interrupt();
    }

}

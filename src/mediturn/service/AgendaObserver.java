package mediturn.service;

import mediturn.model.Turno;

/**
 * Interfaz del patrón Observer.
 * Cualquier clase que quiera "enterarse" cuando un turno está por
 * suceder (para avisar, loguear, etc.) implementa esta interfaz y
 * se suscribe en GestorClinica con suscribir().
 *
 * La idea de Observer: en vez de que GestorClinica sepa "a quién
 * avisar" de forma hardcodeada, cualquier clase nueva que implemente
 * este contrato puede sumarse a la lista de interesados sin tocar
 * el código de GestorClinica. Eso es "bajo acoplamiento".
 */

public interface AgendaObserver {

    /**
     * Se llama cuando GestorClinica detecta que este turno
     * está próximo a ocurrir (lo va a usar RecordatorioThread).
     */
    void actualizar(Turno turno);
    
} 

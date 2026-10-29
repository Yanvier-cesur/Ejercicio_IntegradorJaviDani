package com.cesur.javGdaniM.negocio.excepcion;

/**
 * Excepción que se lanza cuando una fecha no tiene el formato esperado
 * o no es una fecha válida.
 */
public class FechaInvalidaException extends RuntimeException {

    /**
     * Crea una nueva excepción con el mensaje indicado.
     *
     * @param message mensaje descriptivo del error
     */
    public FechaInvalidaException(String message) {
        super(message);
    }
}

package com.cesur.javGdaniM.negocio.excepcion;

/**
 * Excepción que se lanza cuando no se encuentra ninguna candidatura
 * con el identificador indicado.
 */
public class IdNoEncontradoException extends RuntimeException {

    /**
     * Crea una nueva excepción con el mensaje indicado.
     *
     * @param message mensaje descriptivo del error
     */
    public IdNoEncontradoException(String message) {
        super(message);
    }
}

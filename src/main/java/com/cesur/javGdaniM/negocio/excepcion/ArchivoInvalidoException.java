package com.cesur.javGdaniM.negocio.excepcion;

/**
 * Excepción que se lanza cuando un fichero no es válido o no se puede leer,
 * escribir o crear correctamente.
 */
public class ArchivoInvalidoException extends RuntimeException {

    /**
     * Crea una nueva excepción con el mensaje indicado.
     *
     * @param message mensaje descriptivo del error
     */
    public ArchivoInvalidoException(String message) {
        super(message);
    }
}

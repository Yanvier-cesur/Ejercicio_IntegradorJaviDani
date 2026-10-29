package com.cesur.javGdaniM.data.entidad;

/**
 * Estados posibles por los que puede pasar una candidatura de trabajo.
 */
public enum EstadoCandidatura {

    /** La candidatura se ha enviado y está a la espera de respuesta. */
    PENDIENTE,

    /** La candidatura está en proceso de entrevista. */
    ENTREVISTA,

    /** La candidatura ha sido rechazada. */
    RECHAZADO,

    /** La candidatura ha sido aceptada. */
    ACEPTADO;

}

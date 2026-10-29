package com.cesur.javGdaniM.data.entidad;

import java.time.LocalDate;

/**
 * Entidad que representa una candidatura a una oferta de trabajo.
 * <p>
 * Almacena los datos básicos de la aplicación: su identificador, el puesto al que
 * se opta, la empresa, el estado actual de la candidatura y la fecha en la que se realizó.
 */
public class AplicacionDeTrabajo {

    /** Identificador único de la candidatura. */
    public int id;

    /** Título del puesto al que se aplica. */
    public String titulo;

    /** Nombre de la empresa a la que se aplica. */
    public String empresa;

    /** Estado actual de la candidatura. */
    public EstadoCandidatura estadoCandidatura;

    /** Fecha en la que se realizó la candidatura. */
    public LocalDate fecha;

    /**
     * Crea una nueva candidatura con todos sus datos.
     *
     * @param id identificador único de la candidatura
     * @param titulo título del puesto al que se aplica
     * @param empresa nombre de la empresa a la que se aplica
     * @param estadoCandidatura estado inicial de la candidatura
     * @param fecha fecha en la que se realizó la candidatura
     */
    public AplicacionDeTrabajo(int id, String titulo, String empresa, EstadoCandidatura estadoCandidatura, LocalDate fecha) {
        this.id = id;
        this.titulo = titulo;
        this.empresa = empresa;
        this.estadoCandidatura = estadoCandidatura;
        this.fecha = fecha;
    }

    /**
     * Obtiene el identificador único de la candidatura.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador único de la candidatura.
     *
     * @param id nuevo identificador de la candidatura
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el título del puesto al que se aplica.
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Establece el título del puesto al que se aplica.
     *
     * @param titulo nuevo título del puesto
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Obtiene el nombre de la empresa a la que se aplica.
     */
    public String getEmpresa() {
        return empresa;
    }

    /**
     * Establece el nombre de la empresa a la que se aplica.
     *
     * @param empresa nuevo nombre de la empresa
     */
    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    /**
     * Obtiene el estado actual de la candidatura.
     */
    public EstadoCandidatura getEstadoCandidatura() {
        return estadoCandidatura;
    }

    /**
     * Establece el estado actual de la candidatura.
     *
     * @param estadoCandidatura nuevo estado de la candidatura
     */
    public void setEstadoCandidatura(EstadoCandidatura estadoCandidatura) {
        this.estadoCandidatura = estadoCandidatura;
    }

    /**
     * Obtiene la fecha en la que se realizó la candidatura.
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha en la que se realizó la candidatura.
     *
     * @param fecha nueva fecha de la candidatura
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}

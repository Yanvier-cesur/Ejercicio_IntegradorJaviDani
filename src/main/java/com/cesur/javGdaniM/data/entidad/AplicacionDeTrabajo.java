package com.cesur.javGdaniM.data.entidad;

import java.time.LocalDate;

public class AplicacionDeTrabajo {
    int id;
    String titulo;
    String empresa;
    EstadoCandidatura estadoCandidatura;
    LocalDate fecha;

    public AplicacionDeTrabajo(int id, String titulo, String empresa, EstadoCandidatura estadoCandidatura, LocalDate fecha) {
        this.id = id;
        this.titulo = titulo;
        this.empresa = empresa;
        this.estadoCandidatura = estadoCandidatura;
        this.fecha = fecha;
    }
}

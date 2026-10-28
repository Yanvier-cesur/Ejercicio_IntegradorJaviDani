package com.cesur.javGdaniM.data.entidad;

import java.time.LocalDate;

public class AplicacionDeTrabajo {
    public int id;
    public String titulo;
    public String empresa;
     public EstadoCandidatura estadoCandidatura;
   public LocalDate fecha;

    public AplicacionDeTrabajo(int id, String titulo, String empresa, EstadoCandidatura estadoCandidatura, LocalDate fecha) {
        this.id = id;
        this.titulo = titulo;
        this.empresa = empresa;
        this.estadoCandidatura = estadoCandidatura;
        this.fecha = fecha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public EstadoCandidatura getEstadoCandidatura() {
        return estadoCandidatura;
    }

    public void setEstadoCandidatura(EstadoCandidatura estadoCandidatura) {
        this.estadoCandidatura = estadoCandidatura;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}

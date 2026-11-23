package com.cesur.javGdaniM.data.entidad;

import java.time.LocalDate;

public class Oferta {

    private long id;
    private String titulo;
    private Empresa empresa;
    private EstadoCandidatura estadoCandidatura;
    private LocalDate fecha;
    private String enlace;
    private String descripcion;
    private double salario;
    private String notas;

    public Oferta() {
    }

    public Oferta(int id, String titulo, Empresa empresa, EstadoCandidatura estadoCandidatura,
                  LocalDate fecha, String enlace, String descripcion, double salario, String notas) {
        this.id = id;
        this.titulo = titulo;
        this.empresa = empresa;
        this.estadoCandidatura = estadoCandidatura;
        this.fecha = fecha;
        this.enlace = enlace;
        this.descripcion = descripcion;
        this.salario = salario;
        this.notas = notas;
    }

    public int getId() {
        return Math.toIntExact(id);
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

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
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

    public String getEnlace() {
        return enlace;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
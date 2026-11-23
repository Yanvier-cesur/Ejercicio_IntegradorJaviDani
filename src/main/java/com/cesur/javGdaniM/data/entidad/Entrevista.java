package com.cesur.javGdaniM.data.entidad;

import java.time.LocalDate;

public class Entrevista {

    private long id;
    private Oferta oferta;
    private LocalDate fecha;
    private String tipo;
    private String notas;
    private String resultado;

    public Entrevista() {
    }

    public Entrevista(int id, Oferta oferta, LocalDate fecha, String tipo, String notas, String resultado) {
        this.id = id;
        this.oferta = oferta;
        this.fecha = fecha;
        this.tipo = tipo;
        this.notas = notas;
        this.resultado = resultado;
    }

    public int getId() {
        return Math.toIntExact(id);
    }

    public void setId(int id) {
        this.id = id;
    }

    public Oferta getOferta() {
        return oferta;
    }

    public void setOferta(Oferta oferta) {
        this.oferta = oferta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
}
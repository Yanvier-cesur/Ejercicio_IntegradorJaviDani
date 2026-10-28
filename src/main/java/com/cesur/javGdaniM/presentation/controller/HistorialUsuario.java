package com.cesur.javGdaniM.presentation.controller;


import com.cesur.javGdaniM.data.entidad.AplicacionDeTrabajo;
import com.cesur.javGdaniM.data.entidad.EstadoCandidatura;
import com.cesur.javGdaniM.data.repositorio.RepositorioAplicacion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistorialUsuario {

    static void main(String[] args) {

        RepositorioAplicacion repositorio = new RepositorioAplicacion();
        String comando = args[0];

        try {
            switch (comando) {
                case "-listar":
                    Listar(args, repositorio);
                    break;

                case "-añadir":
                    Anadir(args, repositorio);
                    break;

                case "-ver":
                    Ver(args, repositorio);
                    break;

                case "-borrar":
                    Borrar(args, repositorio);
                    break;

                case "-importar":
                    Importar(args, repositorio);
                    break;

                default:
                    System.out.println("Comando desconocido: " + comando);

            }
        } catch (Exception e) {
            System.err.println("Error al ejecutar la operación: " + e.getMessage());
        }
    }

    private static void Listar(String[] args, RepositorioAplicacion repositorio) {
        List<AplicacionDeTrabajo> lista = repositorio.leerFicheroPredeterminado();


        String estadoFiltro = null;
        String empresaFiltro = null;

        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("--estado") && i + 1 < args.length) {
                estadoFiltro = args[i + 1].toUpperCase();
                i++;
            } else if (args[i].equals("--empresa") && i + 1 < args.length) {
                empresaFiltro = args[i + 1];
                i++;
            }
        }

        System.out.println("--- LISTADO DE CANDIDATURAS ---");
        boolean hayResultados = false;
        for (AplicacionDeTrabajo app : lista) {
            boolean cumpleEstado = (estadoFiltro == null || app.getEstadoCandidatura().name().equalsIgnoreCase(estadoFiltro));
            boolean cumpleEmpresa = (empresaFiltro == null || app.getEmpresa().equalsIgnoreCase(empresaFiltro));

            if (cumpleEstado && cumpleEmpresa) {
                System.out.println("ID: " + app.getId() + " | Puesto: " + app.getTitulo() + " | Empresa: " + app.getEmpresa() + " | Estado: " + app.getEstadoCandidatura());
                hayResultados = true;
            }
        }

        if (!hayResultados) {
            System.out.println("No se encontraron candidaturas con los filtros especificados.");
        }
    }

    private static void Anadir(String[] args, RepositorioAplicacion repositorio) {
        String titulo = null;
        String empresa = null;
        EstadoCandidatura estado = EstadoCandidatura.PENDIENTE;
        LocalDate fecha = LocalDate.now();
        int id = (int) (System.currentTimeMillis() % 10000); // Generador simple de ID único

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--titulo":
                    if (i + 1 < args.length) titulo = args[++i];
                    break;
                case "--empresa":
                    if (i + 1 < args.length) empresa = args[++i];
                    break;
                case "--estado":
                    if (i + 1 < args.length) estado = EstadoCandidatura.valueOf(args[++i].toUpperCase());
                    break;
                case "--fecha":
                    if (i + 1 < args.length) fecha = LocalDate.parse(args[++i], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    break;
            }
        }

        if (titulo == null || empresa == null) {
            System.out.println("Error: Faltan parámetros obligatorios (--titulo y --empresa).");
            return;
        }

        AplicacionDeTrabajo nuevaApp = new AplicacionDeTrabajo(id, titulo, empresa, estado, fecha);
        repositorio.guardar(nuevaApp);
        System.out.println("Candidatura añadida correctamente con ID: " + id);
    }

    private static void Ver(String[] args, RepositorioAplicacion repositorio) {
        if (args.length < 2) {
            System.out.println("Error: Debe indicar el ID de la candidatura (ej: -ver 13)");
            return;
        }

        int idBuscado = Integer.parseInt(args[1]);
        List<AplicacionDeTrabajo> lista = repositorio.leerFicheroPredeterminado();

        for (AplicacionDeTrabajo app : lista) {
            if (app.getId() == idBuscado) {
                System.out.println("--- DETALLES DE LA CANDIDATURA ---");
                System.out.println("ID: " + app.getId());
                System.out.println("Título: " + app.getTitulo());
                System.out.println("Empresa: " + app.getEmpresa());
                System.out.println("Estado: " + app.getEstadoCandidatura());
                System.out.println("Fecha: " + app.getFecha());
                return;
            }
        }
        System.out.println("No se encontró ninguna candidatura con el ID: " + idBuscado);
    }

    private static void Borrar(String[] args, RepositorioAplicacion repositorio) {
        if (args.length < 2) {
            System.out.println("Error: Debe indicar el ID de la candidatura a borrar (ej: -borrar 13)");
            return;
        }

        int idBorrar = Integer.parseInt(args[1]);
        List<AplicacionDeTrabajo> lista = repositorio.leerFicheroPredeterminado();
        boolean encontrada = false;

        List<AplicacionDeTrabajo> listaFiltrada = new java.util.ArrayList<>();
        for (AplicacionDeTrabajo app : lista) {
            if (app.getId() == idBorrar) {
                encontrada = true;
            } else {
                listaFiltrada.add(app);
            }
        }

        if (encontrada) {
            repositorio.limpiarArchivo();
            for (AplicacionDeTrabajo app : listaFiltrada) {
                repositorio.guardar(app);
            }
            System.out.println("Candidatura con ID " + idBorrar + " eliminada correctamente.");
        } else {
            System.out.println("No se encontró ninguna candidatura con el ID: " + idBorrar);
        }
    }

    private static void Importar(String[] args, RepositorioAplicacion repositorio) {
        if (args.length < 2) {
            System.out.println("Error: Debe indicar el nombre del archivo CSV a importar (ej: -importar candidaturas.csv)");
            return;
        }

        String ficheroExterno = args[1];
        List<AplicacionDeTrabajo> importadas = repositorio.leerFichero(ficheroExterno);

        for (AplicacionDeTrabajo app : importadas) {
            repositorio.guardar(app);
        }
        System.out.println("Se han importado " + importadas.size() + " candidaturas correctamente desde " + ficheroExterno);
    }


}


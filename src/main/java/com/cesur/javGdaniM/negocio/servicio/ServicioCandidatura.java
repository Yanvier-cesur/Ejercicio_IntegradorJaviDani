package com.cesur.javGdaniM.negocio.servicio;

import com.cesur.javGdaniM.negocio.excepcion.ArchivoInvalidoException;
import com.cesur.javGdaniM.negocio.excepcion.FechaInvalidaException;
import com.cesur.javGdaniM.negocio.excepcion.IdNoEncontradoException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ServicioCandidatura {

    private final String RUTA_ARCHIVO = "candidaturas.txt";

    public void asegurarArchivoExiste() {
        File archivo = new File(RUTA_ARCHIVO);
        try {
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            throw new ArchivoInvalidoException("No se pudo crear o acceder al archivo de candidaturas.");
        }
    }

    public void anadirCandidatura(String titulo, String empresa, String estado, String fechaStr) {
        asegurarArchivoExiste();


        validarFormatoFecha(fechaStr);


        int nuevoId = 1;
        try {
            List<String> lineas = Files.readAllLines(Paths.get(RUTA_ARCHIVO));
            if (!lineas.isEmpty()) {
                String ultimaLinea = lineas.get(lineas.size() - 1);
                String[] partes = ultimaLinea.split(";");
                nuevoId = Integer.parseInt(partes[0]) + 1;
            }
        } catch (IOException e) {
            throw new ArchivoInvalidoException("Error al leer el archivo para calcular el ID.");
        }


        String nuevaLinea = nuevoId + ";" + titulo + ";" + empresa + ";" + estado + ";" + fechaStr;
        try (FileWriter escritor = new FileWriter(RUTA_ARCHIVO, true)) {
            escritor.write(nuevaLinea + "\n");
            System.out.println("¡Candidatura añadida con éxito con ID " + nuevoId + "!");
        } catch (IOException e) {
            throw new ArchivoInvalidoException("Error al escribir en el archivo de candidaturas.");
        }
    }

    public String buscarPorId(int idBuscado) {
        asegurarArchivoExiste();
        try {
            List<String> lineas = Files.readAllLines(Paths.get(RUTA_ARCHIVO));
            for (String linea : lineas) {
                String[] partes = linea.split(";");
                if (partes.length > 0 && Integer.parseInt(partes[0]) == idBuscado) {
                    return linea; // Devuelve la línea completa de esa candidatura
                }
            }
        } catch (IOException e) {
            throw new ArchivoInvalidoException("Error al leer el archivo.");
        }


        throw new IdNoEncontradoException("La candidatura con ID " + idBuscado + " no existe.");
    }


    public List<String> obtenerTodas() {
        asegurarArchivoExiste();
        try {
            return Files.readAllLines(Paths.get(RUTA_ARCHIVO));
        } catch (IOException e) {
            throw new ArchivoInvalidoException("Error al leer las candidaturas.");
        }
    }

    private void validarFormatoFecha(String fechaStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            LocalDate.parse(fechaStr, formatter);
        } catch (DateTimeParseException e) {
            throw new FechaInvalidaException("La fecha '" + fechaStr + "' no es válida. Debe tener el formato dd/MM/yyyy.");
        }
    }
}
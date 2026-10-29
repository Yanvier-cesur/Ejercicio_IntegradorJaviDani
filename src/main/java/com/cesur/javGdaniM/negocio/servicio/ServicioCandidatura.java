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

/**
 * Servicio de negocio para la gestión de candidaturas almacenadas en un fichero de texto.
 * <p>
 * Permite añadir candidaturas, buscarlas por identificador y obtener todas las existentes,
 * validando el formato de las fechas introducidas.
 */
public class ServicioCandidatura {

    /** Ruta del fichero de texto donde se almacenan las candidaturas. */
    private final String RUTA_ARCHIVO = "candidaturas.txt";

    /**
     * Comprueba que el fichero de candidaturas existe y, en caso contrario, lo crea vacío.
     */
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

    /**
     * Añade una nueva candidatura al final del fichero.
     * <p>
     * El identificador se calcula automáticamente a partir de la última candidatura registrada.
     *
     * @param titulo título del puesto al que se aplica
     * @param empresa nombre de la empresa a la que se aplica
     * @param estado estado inicial de la candidatura
     * @param fechaStr fecha de la candidatura en formato "dd/MM/yyyy"
     */
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

    /**
     * Busca una candidatura por su identificador.
     *
     * @param idBuscado identificador de la candidatura que se desea buscar
     */
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


    /**
     * Obtiene todas las candidaturas almacenadas en el fichero.
     */
    public List<String> obtenerTodas() {
        asegurarArchivoExiste();
        try {
            return Files.readAllLines(Paths.get(RUTA_ARCHIVO));
        } catch (IOException e) {
            throw new ArchivoInvalidoException("Error al leer las candidaturas.");
        }
    }

    /**
     * Valida que la fecha indicada cumple el formato "dd/MM/yyyy".
     *
     * @param fechaStr fecha que se desea validar
     */
    private void validarFormatoFecha(String fechaStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            LocalDate.parse(fechaStr, formatter);
        } catch (DateTimeParseException e) {
            throw new FechaInvalidaException("La fecha '" + fechaStr + "' no es válida. Debe tener el formato dd/MM/yyyy.");
        }
    }
}

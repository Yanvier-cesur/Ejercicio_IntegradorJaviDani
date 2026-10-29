package com.cesur.javGdaniM.data.repositorio;

import com.cesur.javGdaniM.data.entidad.AplicacionDeTrabajo;
import com.cesur.javGdaniM.data.entidad.EstadoCandidatura;
import com.cesur.javGdaniM.negocio.excepcion.ArchivoInvalidoException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio encargado de la persistencia de las candidaturas en ficheros CSV.
 * <p>
 * Permite guardar, leer y limpiar las candidaturas, así como convertir entre
 * objetos {@link AplicacionDeTrabajo} y las líneas del fichero.
 */
public class RepositorioAplicacion {

    /** Nombre del fichero CSV utilizado cuando no se indica una ruta personalizada. */
    private static final String FICHERO_PREDETERMINADO = "aplicaciones.csv";

    /** Ruta del fichero CSV con el que trabaja actualmente el repositorio. */
    private String rutaFicheroActual;


    /**
     * Crea un repositorio que trabaja con el fichero CSV predeterminado.
     * <p>
     * Si el fichero no existe, se crea automáticamente.
     */
    public RepositorioAplicacion() {
        this.rutaFicheroActual = FICHERO_PREDETERMINADO;
        asegurarFicheroExiste(this.rutaFicheroActual);
    }

    /**
     * Crea un repositorio que trabaja con un fichero CSV personalizado.
     * <p>
     * Si el fichero no existe, se crea automáticamente.
     *
     * @param rutaPersonalizada ruta o nombre del fichero CSV a utilizar; debe terminar en ".csv"
     *                          y no puede contener ".."
     */
    public RepositorioAplicacion(String rutaPersonalizada) {
        if (validarNombreFichero(rutaPersonalizada)) {
            this.rutaFicheroActual = rutaPersonalizada;
            asegurarFicheroExiste(this.rutaFicheroActual);
        } else {
            throw new ArchivoInvalidoException("El nombre del archivo no es válido: " + rutaPersonalizada);
        }
    }


    /**
     * Comprueba que el nombre de fichero indicado es válido para su uso como CSV.
     * <p>
     * Se considera válido si no está vacío, termina en ".csv" y no contiene "..".
     *
     * @param nombreFichero nombre o ruta del fichero a validar
     */
    public boolean validarNombreFichero(String nombreFichero) {
        if (nombreFichero == null || nombreFichero.trim().isEmpty()) {
            return false;
        }
        String nombreMinuscula = nombreFichero.toLowerCase();
        return nombreMinuscula.endsWith(".csv") && !nombreFichero.contains("..");
    }


    /**
     * Convierte una candidatura en una línea de texto con formato CSV.
     * <p>
     * Los campos que contienen comas, comillas o saltos de línea se escapan entre comillas.
     *
     * @param aplicacion candidatura que se desea convertir
     */
    public String convertirObjetoALinea(AplicacionDeTrabajo aplicacion) {
        return String.format("%d,%s,%s,%s,%s",
                aplicacion.getId(),
                escaparComas(aplicacion.getTitulo()),
                escaparComas(aplicacion.getEmpresa()),
                aplicacion.getEstadoCandidatura().name(),
                aplicacion.getFecha().toString()
        );
    }


    /**
     * Convierte una línea de texto en formato CSV en una candidatura.
     * <p>
     * Si la línea no contiene al menos cinco campos, se devuelve {@code null}.
     *
     * @param linea línea del fichero CSV a convertir
     */
    public AplicacionDeTrabajo convertirLineaAObjeto(String linea) {
        String[] partes = linea.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (partes.length >= 5) {
            return new AplicacionDeTrabajo(
                    Integer.parseInt(quitarComillas(partes[0])),
                    quitarComillas(partes[1]),
                    quitarComillas(partes[2]),
                    EstadoCandidatura.valueOf(quitarComillas(partes[3]).toUpperCase()),
                    LocalDate.parse(quitarComillas(partes[4]))
            );
        }
        return null;
    }


    /**
     * Añade una candidatura al final del fichero CSV actual.
     *
     * @param aplicacion candidatura que se desea guardar
     */
    public void guardar(AplicacionDeTrabajo aplicacion) {
        String linea = convertirObjetoALinea(aplicacion) + System.lineSeparator();
        try {
            Files.write(Paths.get(this.rutaFicheroActual), linea.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new ArchivoInvalidoException("Error al escribir en el fichero CSV: " + this.rutaFicheroActual);
        }
    }


    /**
     * Lee las candidaturas almacenadas en el fichero CSV predeterminado.
     */
    public List<AplicacionDeTrabajo> leerFicheroPredeterminado() {
        return leerFichero(FICHERO_PREDETERMINADO);
    }


    /**
     * Lee las candidaturas almacenadas en un fichero CSV concreto.
     * <p>
     * Si el fichero no existe, se devuelve una lista vacía.
     *
     * @param nombreFichero nombre o ruta del fichero CSV que se desea leer
     */
    public List<AplicacionDeTrabajo> leerFichero(String nombreFichero) {
        if (!validarNombreFichero(nombreFichero)) {
            throw new ArchivoInvalidoException("Nombre de fichero no válido para lectura: " + nombreFichero);
        }

        Path path = Paths.get(nombreFichero);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try {
            List<String> lineas = Files.readAllLines(path);
            List<AplicacionDeTrabajo> lista = new ArrayList<>();
            for (String linea : lineas) {
                if (linea != null && !linea.trim().isEmpty()) {
                    AplicacionDeTrabajo app = convertirLineaAObjeto(linea);
                    if (app != null) {
                        lista.add(app);
                    }
                }
            }
            return lista;
        } catch (IOException e) {
            throw new ArchivoInvalidoException("Error al leer el archivo CSV: " + nombreFichero);
        }
    }


    /**
     * Vacía el contenido del fichero CSV actual sin eliminarlo.
     */
    public void limpiarArchivo() {
        limpiarArchivo(this.rutaFicheroActual);
    }

    /**
     * Vacía el contenido del fichero CSV indicado sin eliminarlo.
     *
     * @param nombreFichero nombre o ruta del fichero CSV que se desea vaciar
     */
    public void limpiarArchivo(String nombreFichero) {
        if (!validarNombreFichero(nombreFichero)) {
            throw new ArchivoInvalidoException("Nombre de fichero no válido para limpieza: " + nombreFichero);
        }
        try {
            Files.write(Paths.get(nombreFichero), new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new ArchivoInvalidoException("Error al limpiar el archivo CSV: " + nombreFichero);
        }
    }


    /**
     * Comprueba si el fichero indicado existe y, en caso contrario, lo crea vacío.
     *
     * @param fichero nombre o ruta del fichero que se desea asegurar
     */
    private void asegurarFicheroExiste(String fichero) {
        Path path = Paths.get(fichero);
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new ArchivoInvalidoException("No se pudo crear el archivo: " + fichero);
            }
        }
    }

    /**
     * Escapa un valor de texto para que pueda almacenarse correctamente en una línea CSV.
     * <p>
     * Si el valor contiene comas, comillas o saltos de línea, se encierra entre comillas
     * y se duplican las comillas internas.
     *
     * @param valor texto que se desea escapar
     */
    private String escaparComas(String valor) {
        if (valor == null) return "";
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    /**
     * Elimina las comillas que envuelven un valor de una línea CSV y recupera las comillas internas.
     *
     * @param valor texto del que se desean quitar las comillas
     */
    private String quitarComillas(String valor) {
        if (valor == null) return "";
        valor = valor.trim();
        if (valor.startsWith("\"") && valor.endsWith("\"")) {
            valor = valor.substring(1, valor.length() - 1);
            valor = valor.replace("\"\"", "\"");
        }
        return valor;
    }
}

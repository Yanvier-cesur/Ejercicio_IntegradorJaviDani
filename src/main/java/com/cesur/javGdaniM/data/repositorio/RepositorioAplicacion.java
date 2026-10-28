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

public class RepositorioAplicacion {

    private static final String FICHERO_PREDETERMINADO = "aplicaciones.csv";
    private String rutaFicheroActual;


    public RepositorioAplicacion() {
        this.rutaFicheroActual = FICHERO_PREDETERMINADO;
        asegurarFicheroExiste(this.rutaFicheroActual);
    }

    public RepositorioAplicacion(String rutaPersonalizada) {
        if (validarNombreFichero(rutaPersonalizada)) {
            this.rutaFicheroActual = rutaPersonalizada;
            asegurarFicheroExiste(this.rutaFicheroActual);
        } else {
            throw new ArchivoInvalidoException("El nombre del archivo no es válido: " + rutaPersonalizada);
        }
    }


    public boolean validarNombreFichero(String nombreFichero) {
        if (nombreFichero == null || nombreFichero.trim().isEmpty()) {
            return false;
        }
        String nombreMinuscula = nombreFichero.toLowerCase();
        return nombreMinuscula.endsWith(".csv") && !nombreFichero.contains("..");
    }


    public String convertirObjetoALinea(AplicacionDeTrabajo aplicacion) {
        return String.format("%d,%s,%s,%s,%s",
                aplicacion.getId(),
                escaparComas(aplicacion.getTitulo()),
                escaparComas(aplicacion.getEmpresa()),
                aplicacion.getEstadoCandidatura().name(),
                aplicacion.getFecha().toString()
        );
    }


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


    public void guardar(AplicacionDeTrabajo aplicacion) {
        String linea = convertirObjetoALinea(aplicacion) + System.lineSeparator();
        try {
            Files.write(Paths.get(this.rutaFicheroActual), linea.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new ArchivoInvalidoException("Error al escribir en el fichero CSV: " + this.rutaFicheroActual);
        }
    }


    public List<AplicacionDeTrabajo> leerFicheroPredeterminado() {
        return leerFichero(FICHERO_PREDETERMINADO);
    }


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


    public void limpiarArchivo() {
        limpiarArchivo(this.rutaFicheroActual);
    }

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

    private String escaparComas(String valor) {
        if (valor == null) return "";
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

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
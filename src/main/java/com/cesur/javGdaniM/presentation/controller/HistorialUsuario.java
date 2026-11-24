package com.cesur.javGdaniM.presentation.controller;

import com.cesur.javGdaniM.data.entidad.Empresa;
import com.cesur.javGdaniM.data.entidad.EstadoCandidatura;
import com.cesur.javGdaniM.data.entidad.Oferta;
import com.cesur.javGdaniM.negocio.excepcion.ArchivoInvalidoException;
import com.cesur.javGdaniM.negocio.excepcion.FechaInvalidaException;
import com.cesur.javGdaniM.negocio.excepcion.IdNoEncontradoException;
import com.cesur.javGdaniM.negocio.servicio.ServicioCandidatura;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador JavaFX de ejemplo para conectar la vista de candidaturas
 * con {@link ServicioCandidatura} en la versión 2.0.
 */
public class HistorialUsuario {

    private static final DateTimeFormatter FORMATO_UI = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ServicioCandidatura servicioCandidatura;
    private final ObservableList<Oferta> ofertasObservable = FXCollections.observableArrayList();

    @FXML
    private TableView<Oferta> tablaOfertas;

    @FXML
    private TableColumn<Oferta, Integer> colId;

    @FXML
    private TableColumn<Oferta, String> colTitulo;

    @FXML
    private TableColumn<Oferta, String> colEmpresa;

    @FXML
    private TableColumn<Oferta, String> colEstado;

    @FXML
    private TableColumn<Oferta, String> colFecha;

    @FXML
    private TextField txtTitulo;

    @FXML
    private ComboBox<Empresa> comboEmpresa;

    @FXML
    private ComboBox<EstadoCandidatura> comboEstado;

    @FXML
    private DatePicker dateFecha;

    public HistorialUsuario() {
        this.servicioCandidatura = new ServicioCandidatura();
    }

    public HistorialUsuario(ServicioCandidatura servicioCandidatura) {
        this.servicioCandidatura = servicioCandidatura;
    }

    @FXML
    public void initialize() {
        configurarTabla();
        configurarCombos();
        cargarDatosIniciales();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colEmpresa.setCellValueFactory(cellData -> {
            Oferta oferta = cellData.getValue();
            String nombreEmpresa = (oferta != null && oferta.getEmpresa() != null && oferta.getEmpresa().nombre != null)
                    ? oferta.getEmpresa().nombre
                    : "";
            return new SimpleStringProperty(nombreEmpresa);
        });
        colEstado.setCellValueFactory(cellData -> {
            Oferta oferta = cellData.getValue();
            String estado = (oferta != null && oferta.getEstadoCandidatura() != null)
                    ? oferta.getEstadoCandidatura().name()
                    : "";
            return new SimpleStringProperty(estado);
        });
        colFecha.setCellValueFactory(cellData -> {
            Oferta oferta = cellData.getValue();
            String fechaTexto = (oferta != null && oferta.getFecha() != null)
                    ? oferta.getFecha().format(FORMATO_UI)
                    : "";
            return new SimpleStringProperty(fechaTexto);
        });
        tablaOfertas.setItems(ofertasObservable);
    }

    private void configurarCombos() {
        comboEstado.setItems(FXCollections.observableArrayList(EstadoCandidatura.values()));
        if (!comboEstado.getItems().isEmpty() && comboEstado.getValue() == null) {
            comboEstado.setValue(EstadoCandidatura.PENDIENTE);
        }
        if (dateFecha.getValue() == null) {
            dateFecha.setValue(LocalDate.now());
        }
    }

    @FXML
    public void cargarDatosIniciales() {
        try {
            List<Empresa> empresas = servicioCandidatura.obtenerEmpresas();
            comboEmpresa.setItems(FXCollections.observableArrayList(empresas));

            List<Oferta> ofertas = servicioCandidatura.obtenerTodas();
            ofertasObservable.setAll(ofertas);
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de carga",
                    "No se pudieron cargar los datos desde la base de datos: " + e.getMessage());
        }
    }

    @FXML
    public void onGuardarOferta() {
        try {
            Empresa empresaSeleccionada = comboEmpresa.getValue();
            if (empresaSeleccionada == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Empresa requerida",
                        "Selecciona una empresa antes de guardar la candidatura.");
                return;
            }

            String fechaFormateada = dateFecha.getValue() != null
                    ? dateFecha.getValue().format(FORMATO_UI)
                    : LocalDate.now().format(FORMATO_UI);

            servicioCandidatura.anadirCandidatura(
                    txtTitulo.getText(),
                    empresaSeleccionada.id,
                    comboEstado.getValue(),
                    fechaFormateada
            );

            limpiarFormulario();
            cargarDatosIniciales();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Guardado",
                    "La candidatura se ha registrado correctamente.");
        } catch (FechaInvalidaException | IdNoEncontradoException | IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Datos no válidos", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar",
                    "No se pudo guardar la candidatura: " + e.getMessage());
        }
    }

    @FXML
    public void onMigrarCsv() {
        try {
            int migradas = servicioCandidatura.migrarDesdeCsv("aplicaciones.csv");
            cargarDatosIniciales();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Migración completada",
                    "Se han migrado " + migradas + " candidaturas desde aplicaciones.csv a MySQL.");
        } catch (ArchivoInvalidoException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Error de archivo CSV", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error en la migración",
                    "No se pudo completar la migración: " + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtTitulo.clear();
        dateFecha.setValue(LocalDate.now());
        comboEstado.setValue(EstadoCandidatura.PENDIENTE);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
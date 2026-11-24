package com.cesur.javGdaniM.presentation.controller;

import com.cesur.javGdaniM.data.entidad.Entrevista;
import com.cesur.javGdaniM.data.entidad.EstadoCandidatura;
import com.cesur.javGdaniM.data.entidad.Oferta;
import com.cesur.javGdaniM.data.repositorio.EntrevistaJdbcDAO;
import com.cesur.javGdaniM.negocio.excepcion.IdNoEncontradoException;
import com.cesur.javGdaniM.negocio.servicio.ServicioCandidatura;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class EntrevistaController {

    @FXML private Label lblInfoOferta;
    @FXML private Label lblEstadoOferta;

    @FXML private TableView<Entrevista> tablaEntrevistas;
    @FXML private TableColumn<Entrevista, Integer> colId;
    @FXML private TableColumn<Entrevista, LocalDate> colFecha;
    @FXML private TableColumn<Entrevista, String> colTipo;
    @FXML private TableColumn<Entrevista, String> colResultado;
    @FXML private TableColumn<Entrevista, String> colNotas;

    @FXML private DatePicker dateFecha;
    @FXML private ComboBox<String> comboTipo;
    @FXML private ComboBox<String> comboResultado;
    @FXML private ComboBox<EstadoCandidatura> comboNuevoEstado;
    @FXML private TextArea txtNotas;

    @FXML private Button btnGuardar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnCerrar;

    private final ServicioCandidatura servicio = new ServicioCandidatura();
    private final EntrevistaJdbcDAO entrevistaJdbcDAO = new EntrevistaJdbcDAO();
    private final ObservableList<Entrevista> entrevistasObs = FXCollections.observableArrayList();

    private Oferta ofertaActual;

    @FXML
    public void initialize() {
        configurarTabla();
        configurarCombos();
        dateFecha.setValue(LocalDate.now());
    }

    public void setOferta(Oferta oferta) {
        this.ofertaActual = oferta;
        actualizarCabeceraOferta();
        refrescarEntrevistas();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        colFecha.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getFecha()));
        colTipo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTipo()));
        colResultado.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getResultado()));
        colNotas.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNotas()));
        tablaEntrevistas.setItems(entrevistasObs);
    }

    private void configurarCombos() {
        comboTipo.setItems(FXCollections.observableArrayList(
                "Inicial / RR. HH.",
                "Técnica",
                "Prueba técnica / Live coding",
                "Cultural / Manager",
                "Final"
        ));

        comboResultado.setItems(FXCollections.observableArrayList(
                "Pendiente",
                "Superada",
                "En proceso",
                "No superada"
        ));

        comboNuevoEstado.setItems(FXCollections.observableArrayList(EstadoCandidatura.values()));
    }

    private void actualizarCabeceraOferta() {
        if (ofertaActual == null) {
            lblInfoOferta.setText("Oferta: -");
            lblEstadoOferta.setText("Empresa: - | Estado: -");
            return;
        }
        String nombreEmpresa = (ofertaActual.getEmpresa() != null && ofertaActual.getEmpresa().nombre != null)
                ? ofertaActual.getEmpresa().nombre
                : "Sin empresa";
        String estado = ofertaActual.getEstadoCandidatura() != null
                ? ofertaActual.getEstadoCandidatura().name()
                : "Sin estado";

        lblInfoOferta.setText("Oferta #" + ofertaActual.getId() + ": " + ofertaActual.getTitulo());
        lblEstadoOferta.setText("Empresa: " + nombreEmpresa + " | Estado actual: " + estado);
    }

    private void refrescarEntrevistas() {
        if (ofertaActual == null) {
            entrevistasObs.clear();
            return;
        }
        List<Entrevista> lista = entrevistaJdbcDAO.findByOfertaId(ofertaActual.getId());
        entrevistasObs.setAll(lista);
    }

    @FXML
    private void onGuardarEntrevista() {
        if (ofertaActual == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "No hay ninguna oferta seleccionada.");
            return;
        }

        try {
            LocalDate fecha = dateFecha.getValue() != null ? dateFecha.getValue() : LocalDate.now();
            String tipo = comboTipo.getEditor().getText() != null ? comboTipo.getEditor().getText().trim() : "";
            String resultado = comboResultado.getEditor().getText() != null ? comboResultado.getEditor().getText().trim() : "";
            String notas = txtNotas.getText() != null ? txtNotas.getText().trim() : "";
            EstadoCandidatura nuevoEstado = comboNuevoEstado.getValue();

            if (tipo.isBlank()) {
                throw new IllegalArgumentException("Indica el tipo de entrevista.");
            }
            if (resultado.isBlank()) {
                throw new IllegalArgumentException("Indica el resultado de la entrevista.");
            }

            servicio.registrarEntrevista(
                    ofertaActual.getId(),
                    fecha,
                    tipo,
                    notas,
                    resultado,
                    nuevoEstado
            );


            ofertaActual = servicio.buscarPorId(ofertaActual.getId());
            actualizarCabeceraOferta();
            refrescarEntrevistas();
            onLimpiarCampos();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Entrevista registrada correctamente.");
        } catch (IdNoEncontradoException | IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al registrar entrevista: " + e.getMessage());
        }
    }

    @FXML
    private void onLimpiarCampos() {
        dateFecha.setValue(LocalDate.now());
        comboTipo.setValue(null);
        comboTipo.getEditor().clear();
        comboResultado.setValue(null);
        comboResultado.getEditor().clear();
        comboNuevoEstado.setValue(null);
        txtNotas.clear();
    }

    @FXML
    private void onCerrar() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Entrevistas");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
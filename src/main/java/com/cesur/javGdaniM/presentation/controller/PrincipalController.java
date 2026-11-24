package com.cesur.javGdaniM.presentation.controller;

import com.cesur.javGdaniM.data.entidad.Empresa;
import com.cesur.javGdaniM.data.entidad.EstadoCandidatura;
import com.cesur.javGdaniM.data.entidad.Oferta;
import com.cesur.javGdaniM.data.repositorio.OfertaJdbcDAO;
import com.cesur.javGdaniM.negocio.excepcion.FechaInvalidaException;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class PrincipalController {

    @FXML private Button btnMigrarCsv;
    @FXML private ComboBox<Empresa> comboFiltroEmpresa;
    @FXML private ComboBox<EstadoCandidatura> comboFiltroEstado;
    @FXML private TextField txtFiltroSalarioMin;
    @FXML private DatePicker dateFiltroDesde;
    @FXML private Button btnFiltrar;
    @FXML private Button btnLimpiarFiltros;

    @FXML private TableView<Oferta> tablaOfertas;
    @FXML private TableColumn<Oferta, Integer> colId;
    @FXML private TableColumn<Oferta, String> colTitulo;
    @FXML private TableColumn<Oferta, String> colEmpresa;
    @FXML private TableColumn<Oferta, EstadoCandidatura> colEstado;
    @FXML private TableColumn<Oferta, LocalDate> colFecha;
    @FXML private TableColumn<Oferta, Double> colSalario;

    @FXML private TextField txtTitulo;
    @FXML private ComboBox<Empresa> comboEmpresa;
    @FXML private ComboBox<EstadoCandidatura> comboEstado;
    @FXML private DatePicker dateFecha;
    @FXML private TextField txtSalario;
    @FXML private TextField txtEnlace;
    @FXML private TextArea txtDescripcion;
    @FXML private TextArea txtNotas;

    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnEntrevistas;

    private final ServicioCandidatura servicio = new ServicioCandidatura();
    private final OfertaJdbcDAO ofertaJdbcDAO = new OfertaJdbcDAO();
    private final ObservableList<Oferta> ofertasObs = FXCollections.observableArrayList();
    private Oferta ofertaSeleccionada;

    @FXML
    public void initialize() {
        configurarTabla();
        configurarCombos();
        configurarSeleccionTabla();
        refrescarEmpresas();
        refrescarOfertas(servicio.obtenerTodas());
    }

    private void configurarTabla() {
        colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        colTitulo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitulo()));
        colEmpresa.setCellValueFactory(cell -> {
            Empresa emp = cell.getValue().getEmpresa();
            return new SimpleStringProperty(emp != null ? emp.nombre : "");
        });
        colEstado.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getEstadoCandidatura()));
        colFecha.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getFecha()));
        colSalario.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getSalario()));
        tablaOfertas.setItems(ofertasObs);
    }

    private void configurarCombos() {
        ObservableList<EstadoCandidatura> estados = FXCollections.observableArrayList(EstadoCandidatura.values());
        comboEstado.setItems(estados);
        comboFiltroEstado.setItems(estados);

        configurarCeldaEmpresa(comboEmpresa);
        configurarCeldaEmpresa(comboFiltroEmpresa);
    }

    private void configurarCeldaEmpresa(ComboBox<Empresa> combo) {
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Empresa item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.nombre);
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Empresa item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.nombre);
            }
        });
    }

    private void configurarSeleccionTabla() {
        tablaOfertas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            ofertaSeleccionada = newVal;
            if (newVal != null) {
                cargarOfertaEnFormulario(newVal);
            }
        });
    }

    private void cargarOfertaEnFormulario(Oferta oferta) {
        txtTitulo.setText(oferta.getTitulo());
        comboEstado.setValue(oferta.getEstadoCandidatura());
        dateFecha.setValue(oferta.getFecha());
        txtSalario.setText(oferta.getSalario() > 0 ? String.valueOf(oferta.getSalario()) : "");
        txtEnlace.setText(oferta.getEnlace());
        txtDescripcion.setText(oferta.getDescripcion());
        txtNotas.setText(oferta.getNotas());

        if (oferta.getEmpresa() != null) {
            for (Empresa emp : comboEmpresa.getItems()) {
                if (emp.id == oferta.getEmpresa().id) {
                    comboEmpresa.setValue(emp);
                    break;
                }
            }
        } else {
            comboEmpresa.setValue(null);
        }
    }

    private double parsearSalario(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El salario debe ser un número válido.");
        }
    }

    @FXML
    private void onGuardarOferta() {
        try {
            String titulo = txtTitulo.getText() != null ? txtTitulo.getText().trim() : "";
            Empresa empresa = comboEmpresa.getValue();
            if (empresa == null) {
                throw new IllegalArgumentException("Debes seleccionar una empresa.");
            }
            EstadoCandidatura estado = comboEstado.getValue();
            LocalDate fecha = dateFecha.getValue() != null ? dateFecha.getValue() : LocalDate.now();
            double salario = parsearSalario(txtSalario.getText());
            String enlace = txtEnlace.getText() != null ? txtEnlace.getText().trim() : "";
            String descripcion = txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "";
            String notas = txtNotas.getText() != null ? txtNotas.getText().trim() : "";

            Oferta creada = servicio.anadirCandidatura(titulo, empresa.id, estado, fecha);

            boolean tieneDetallesExtra = salario > 0.0 || !enlace.isBlank() || !descripcion.isBlank() || !notas.isBlank();
            if (tieneDetallesExtra) {
                creada.setSalario(salario);
                creada.setEnlace(enlace);
                creada.setDescripcion(descripcion);
                creada.setNotas(notas);
                ofertaJdbcDAO.update(creada);
            }

            refrescarOfertas(servicio.obtenerTodas());
            onLimpiarFormulario();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Oferta guardada correctamente.");
        } catch (FechaInvalidaException | IdNoEncontradoException | IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar la oferta: " + e.getMessage());
        }
    }

    @FXML
    private void onActualizarOferta() {
        if (ofertaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecciona primero una oferta de la tabla.");
            return;
        }
        try {
            String titulo = txtTitulo.getText() != null ? txtTitulo.getText().trim() : "";
            if (titulo.isBlank()) {
                throw new IllegalArgumentException("El título de la oferta es obligatorio.");
            }
            Empresa empresa = comboEmpresa.getValue();
            if (empresa == null) {
                throw new IllegalArgumentException("Debes seleccionar una empresa.");
            }
            EstadoCandidatura estado = comboEstado.getValue() != null
                    ? comboEstado.getValue()
                    : EstadoCandidatura.PENDIENTE;
            LocalDate fecha = dateFecha.getValue() != null ? dateFecha.getValue() : LocalDate.now();
            double salario = parsearSalario(txtSalario.getText());

            Oferta actualizada = new Oferta();
            actualizada.setId(ofertaSeleccionada.getId());
            actualizada.setTitulo(titulo);
            actualizada.setEmpresa(empresa);
            actualizada.setEstadoCandidatura(estado);
            actualizada.setFecha(fecha);
            actualizada.setSalario(salario);
            actualizada.setEnlace(txtEnlace.getText() != null ? txtEnlace.getText().trim() : "");
            actualizada.setDescripcion(txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "");
            actualizada.setNotas(txtNotas.getText() != null ? txtNotas.getText().trim() : "");

            ofertaJdbcDAO.update(actualizada);
            refrescarOfertas(servicio.obtenerTodas());
            mostrarAlerta(Alert.AlertType.INFORMATION, "Oferta actualizada correctamente.");
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al actualizar la oferta: " + e.getMessage());
        }
    }

    @FXML
    private void onEliminarOferta() {
        if (ofertaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecciona primero una oferta de la tabla.");
            return;
        }
        try {
            servicio.buscarPorId(ofertaSeleccionada.getId());
            ofertaJdbcDAO.deleteById((long) ofertaSeleccionada.getId());
            refrescarOfertas(servicio.obtenerTodas());
            onLimpiarFormulario();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Oferta eliminada correctamente.");
        } catch (IdNoEncontradoException | IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al eliminar la oferta: " + e.getMessage());
        }
    }

    @FXML
    private void onFiltrar() {
        try {
            Long empresaId = comboFiltroEmpresa.getValue() != null ? comboFiltroEmpresa.getValue().id : null;
            EstadoCandidatura estado = comboFiltroEstado.getValue();
            LocalDate desdeFecha = dateFiltroDesde.getValue();

            Double salarioMin = null;
            String salTexto = txtFiltroSalarioMin.getText() != null ? txtFiltroSalarioMin.getText().trim() : "";
            if (!salTexto.isBlank()) {
                salarioMin = Double.parseDouble(salTexto.replace(",", "."));
            }

            List<Oferta> filtradas = ofertaJdbcDAO.search(empresaId, estado, salarioMin, desdeFecha);
            refrescarOfertas(filtradas);
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "El filtro de salario mínimo debe ser numérico.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al filtrar ofertas: " + e.getMessage());
        }
    }

    @FXML
    private void onLimpiarFiltros() {
        comboFiltroEmpresa.setValue(null);
        comboFiltroEstado.setValue(null);
        txtFiltroSalarioMin.clear();
        dateFiltroDesde.setValue(null);
        refrescarOfertas(servicio.obtenerTodas());
    }

    @FXML
    private void onLimpiarFormulario() {
        ofertaSeleccionada = null;
        tablaOfertas.getSelectionModel().clearSelection();
        txtTitulo.clear();
        comboEmpresa.setValue(null);
        comboEstado.setValue(null);
        dateFecha.setValue(null);
        txtSalario.clear();
        txtEnlace.clear();
        txtDescripcion.clear();
        txtNotas.clear();
    }

    @FXML
    private void onMigrarCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar CSV de candidaturas");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        File archivo = fileChooser.showOpenDialog(btnMigrarCsv.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        try {
            int migradas = servicio.migrarDesdeCsv(archivo.getAbsolutePath());
            refrescarEmpresas();
            refrescarOfertas(servicio.obtenerTodas());
            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Migración desde CSV completada. Ofertas importadas: " + migradas);
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error durante la migración CSV: " + e.getMessage());
        }
    }

    @FXML
    private void onAbrirEntrevistas() {
        if (ofertaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecciona una oferta para gestionar sus entrevistas.");
            return;
        }
        mostrarAlerta(Alert.AlertType.INFORMATION,
                "Oferta seleccionada: " + ofertaSeleccionada.getTitulo() + " (ID " + ofertaSeleccionada.getId() + ")");
    }

    private void refrescarEmpresas() {
        List<Empresa> empresas = servicio.obtenerEmpresas();
        comboEmpresa.setItems(FXCollections.observableArrayList(empresas));
        comboFiltroEmpresa.setItems(FXCollections.observableArrayList(empresas));
        System.out.println("Empresas: " + servicio.obtenerEmpresas().size());
    }

    private void refrescarOfertas(List<Oferta> ofertas) {
        ofertasObs.setAll(ofertas);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Gestión de Candidaturas");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
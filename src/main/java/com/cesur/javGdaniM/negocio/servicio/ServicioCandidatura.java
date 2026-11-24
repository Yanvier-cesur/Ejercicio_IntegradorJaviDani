package com.cesur.javGdaniM.negocio.servicio;

import com.cesur.javGdaniM.data.entidad.Empresa;
import com.cesur.javGdaniM.data.entidad.Entrevista;
import com.cesur.javGdaniM.data.entidad.EstadoCandidatura;
import com.cesur.javGdaniM.data.entidad.Oferta;
import com.cesur.javGdaniM.data.repositorio.DataSourceFactory;
import com.cesur.javGdaniM.data.repositorio.EmpresaJdbcDAO;
import com.cesur.javGdaniM.data.repositorio.EntrevistaJdbcDAO;
import com.cesur.javGdaniM.data.repositorio.OfertaJdbcDAO;
import com.cesur.javGdaniM.data.repositorio.RepositorioAplicacion;
import com.cesur.javGdaniM.data.repositorio.TransactionalDAO;
import com.cesur.javGdaniM.negocio.excepcion.FechaInvalidaException;
import com.cesur.javGdaniM.negocio.excepcion.IdNoEncontradoException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Servicio de negocio para la gestión de candidaturas (ofertas, empresas y entrevistas)
 * respaldado por MySQL mediante DAOs JDBC y soporte transaccional.
 */
public class ServicioCandidatura {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TransactionalDAO<Empresa> empresaDAO;
    private final TransactionalDAO<Oferta> ofertaDAO;
    private final TransactionalDAO<Entrevista> entrevistaDAO;

    public ServicioCandidatura() {
        this.empresaDAO = new EmpresaJdbcDAO();
        this.ofertaDAO = new OfertaJdbcDAO();
        this.entrevistaDAO = new EntrevistaJdbcDAO();
    }

    public ServicioCandidatura(
            TransactionalDAO<Empresa> empresaDAO,
            TransactionalDAO<Oferta> ofertaDAO,
            TransactionalDAO<Entrevista> entrevistaDAO) {
        this.empresaDAO = empresaDAO;
        this.ofertaDAO = ofertaDAO;
        this.entrevistaDAO = entrevistaDAO;
    }

    /**
     * Crea una nueva oferta asociada a una empresa existente (por ID).
     */
    public Oferta anadirCandidatura(String titulo, long empresaId, EstadoCandidatura estado, LocalDate fecha) {
        validarTitulo(titulo);
        LocalDate fechaEfectiva = fecha != null ? fecha : LocalDate.now();
        EstadoCandidatura estadoEfectivo = estado != null ? estado : EstadoCandidatura.PENDIENTE;

        Empresa empresa = empresaDAO.findById(empresaId)
                .orElseThrow(() -> new IdNoEncontradoException("No existe la empresa con ID " + empresaId));

        Oferta oferta = new Oferta();
        oferta.setTitulo(titulo.trim());
        oferta.setEmpresa(empresa);
        oferta.setEstadoCandidatura(estadoEfectivo);
        oferta.setFecha(fechaEfectiva);
        oferta.setEnlace("");
        oferta.setDescripcion("");
        oferta.setSalario(0.0);
        oferta.setNotas("");

        return ofertaDAO.save(oferta);
    }

    /**
     * Sobrecarga útil si recibes la fecha como texto en formato "dd/MM/yyyy".
     */
    public Oferta anadirCandidatura(String titulo, long empresaId, EstadoCandidatura estado, String fechaStr) {
        return anadirCandidatura(titulo, empresaId, estado, parsearFecha(fechaStr));
    }

    /**
     * Crea una empresa y una oferta inicial en una sola transacción:
     * si falla la oferta, se hace rollback y no se guarda la empresa a medias.
     */
    public Oferta crearEmpresaYOferta(Empresa empresa, Oferta oferta) {
        if (empresa == null || empresa.nombre == null || empresa.nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio.");
        }
        validarTitulo(oferta != null ? oferta.getTitulo() : null);

        if (empresa.fechaRegistro == null) {
            empresa.fechaRegistro = LocalDate.now();
        }
        if (oferta.getFecha() == null) {
            oferta.setFecha(LocalDate.now());
        }
        if (oferta.getEstadoCandidatura() == null) {
            oferta.setEstadoCandidatura(EstadoCandidatura.PENDIENTE);
        }

        try (Connection conn = DataSourceFactory.getDataSource().getConnection()) {
            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Empresa empresaGuardada = empresaDAO.save(conn, empresa);
                oferta.setEmpresa(empresaGuardada);
                Oferta ofertaGuardada = ofertaDAO.save(conn, oferta);
                conn.commit();
                return ofertaGuardada;
            } catch (Exception e) {
                rollbackSeguro(conn, e);
                throw new RuntimeException("Error al crear empresa y oferta; se ha realizado rollback.", e);
            } finally {
                restaurarAutoCommit(conn, autoCommitOriginal);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión al crear empresa y oferta.", e);
        }
    }

    /**
     * Registra una entrevista y actualiza el estado de la oferta asociada en una misma transacción.
     */
    public Entrevista registrarEntrevista(int ofertaId, LocalDate fecha, String tipo, String notas,
                                          String resultado, EstadoCandidatura nuevoEstadoOferta) {
        if (ofertaId <= 0) {
            throw new IdNoEncontradoException("El ID de oferta " + ofertaId + " no es válido.");
        }

        try (Connection conn = DataSourceFactory.getDataSource().getConnection()) {
            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Oferta oferta = ofertaDAO.findById(conn, (long) ofertaId)
                        .orElseThrow(() -> new IdNoEncontradoException("No existe la oferta con ID " + ofertaId));

                Entrevista entrevista = new Entrevista();
                entrevista.setOferta(oferta);
                entrevista.setFecha(fecha != null ? fecha : LocalDate.now());
                entrevista.setTipo(tipo);
                entrevista.setNotas(notas);
                entrevista.setResultado(resultado);

                Entrevista guardada = entrevistaDAO.save(conn, entrevista);

                if (nuevoEstadoOferta != null) {
                    oferta.setEstadoCandidatura(nuevoEstadoOferta);
                    ofertaDAO.update(conn, oferta);
                }

                conn.commit();
                return guardada;
            } catch (Exception e) {
                rollbackSeguro(conn, e);
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException("Error al registrar entrevista; se ha realizado rollback.", e);
            } finally {
                restaurarAutoCommit(conn, autoCommitOriginal);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión al registrar entrevista.", e);
        }
    }

    public Oferta buscarPorId(int idBuscado) {
        return ofertaDAO.findById((long) idBuscado)
                .orElseThrow(() -> new IdNoEncontradoException("La candidatura con ID " + idBuscado + " no existe."));
    }

    public List<Oferta> obtenerTodas() {
        return ofertaDAO.findAll();
    }

    public List<Empresa> obtenerEmpresas() {
        return empresaDAO.findAll();
    }

    /**
     * Migra candidaturas desde un CSV de la v1.0 hacia MySQL, reutilizando empresas
     * si ya existen por nombre y devolviendo cuántas ofertas se importaron.
     */
    public int migrarDesdeCsv(String rutaCsv) {
        RepositorioAplicacion repoCsv = new RepositorioAplicacion(rutaCsv);
        List<Oferta> ofertasCsv = repoCsv.leerFichero(rutaCsv);
        List<Empresa> empresasExistentes = empresaDAO.findAll();

        int migradas = 0;
        for (Oferta ofertaCsv : ofertasCsv) {
            String nombreEmpresa = (ofertaCsv.getEmpresa() != null && ofertaCsv.getEmpresa().nombre != null
                    && !ofertaCsv.getEmpresa().nombre.isBlank())
                    ? ofertaCsv.getEmpresa().nombre.trim()
                    : "Empresa sin nombre";

            Empresa empresaDestino = empresasExistentes.stream()
                    .filter(e -> e.nombre != null && e.nombre.equalsIgnoreCase(nombreEmpresa))
                    .findFirst()
                    .orElseGet(() -> {
                        Empresa nueva = new Empresa();
                        nueva.nombre = nombreEmpresa;
                        nueva.sector = "";
                        nueva.web = "";
                        nueva.fechaRegistro = ofertaCsv.getFecha() != null ? ofertaCsv.getFecha() : LocalDate.now();
                        Empresa guardada = empresaDAO.save(nueva);
                        empresasExistentes.add(guardada);
                        return guardada;
                    });

            Oferta nuevaOferta = new Oferta();
            nuevaOferta.setTitulo(ofertaCsv.getTitulo());
            nuevaOferta.setEmpresa(empresaDestino);
            nuevaOferta.setEstadoCandidatura(
                    ofertaCsv.getEstadoCandidatura() != null ? ofertaCsv.getEstadoCandidatura() : EstadoCandidatura.PENDIENTE
            );
            nuevaOferta.setFecha(ofertaCsv.getFecha() != null ? ofertaCsv.getFecha() : LocalDate.now());
            nuevaOferta.setEnlace(ofertaCsv.getEnlace() != null ? ofertaCsv.getEnlace() : "");
            nuevaOferta.setDescripcion(ofertaCsv.getDescripcion() != null ? ofertaCsv.getDescripcion() : "");
            nuevaOferta.setSalario(ofertaCsv.getSalario());
            nuevaOferta.setNotas(ofertaCsv.getNotas() != null ? ofertaCsv.getNotas() : "");

            ofertaDAO.save(nuevaOferta);
            migradas++;
        }
        return migradas;
    }

    private void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título de la oferta no puede estar vacío.");
        }
    }

    private LocalDate parsearFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) {
            throw new FechaInvalidaException("La fecha no puede estar vacía. Debe tener el formato dd/MM/yyyy.");
        }
        try {
            return LocalDate.parse(fechaStr.trim(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new FechaInvalidaException(
                    "La fecha '" + fechaStr + "' no es válida. Debe tener el formato dd/MM/yyyy."
            );
        }
    }

    private void rollbackSeguro(Connection conn, Exception causa) {
        try {
            conn.rollback();
        } catch (SQLException rollbackEx) {
            causa.addSuppressed(rollbackEx);
        }
    }

    private void restaurarAutoCommit(Connection conn, boolean autoCommitOriginal) {
        try {
            conn.setAutoCommit(autoCommitOriginal);
        } catch (SQLException ignored) {
        }
    }
}
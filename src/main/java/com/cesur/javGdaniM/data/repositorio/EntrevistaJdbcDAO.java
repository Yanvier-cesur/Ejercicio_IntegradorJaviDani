package com.cesur.javGdaniM.data.repositorio;

import com.cesur.javGdaniM.data.entidad.Empresa;
import com.cesur.javGdaniM.data.entidad.Entrevista;
import com.cesur.javGdaniM.data.entidad.EstadoCandidatura;
import com.cesur.javGdaniM.data.entidad.Oferta;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntrevistaJdbcDAO implements TransactionalDAO<Entrevista> {

    private static final String BASE_SELECT_WITH_OFERTA =
            "SELECT en.id, en.id_oferta, en.fecha, en.tipo, en.notas, en.resultado, " +
                    "o.id_empresa, o.titulo AS oferta_titulo, o.descripcion AS oferta_descripcion, " +
                    "o.estado AS oferta_estado, o.fecha_postulacion AS oferta_fecha, " +
                    "o.enlace AS oferta_enlace, o.salario AS oferta_salario, o.notas AS oferta_notas, " +
                    "e.nombre AS empresa_nombre, e.sector AS empresa_sector, " +
                    "e.web AS empresa_web, e.fecha_registro AS empresa_fecha_registro " +
                    "FROM entrevista en " +
                    "LEFT JOIN oferta o ON en.id_oferta = o.id " +
                    "LEFT JOIN empresa e ON o.id_empresa = e.id";

    @Override
    public Optional<Entrevista> findById(Long id) {
        try (Connection conn = DataSourceFactory.getDataSource().getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando entrevista con id " + id, e);
        }
    }

    @Override
    public Optional<Entrevista> findById(Connection conn, Long id) {
        String sql = BASE_SELECT_WITH_OFERTA + " WHERE en.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowWithOferta(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando entrevista con id " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Entrevista> findAll() {
        String sql = BASE_SELECT_WITH_OFERTA + " ORDER BY en.fecha DESC, en.id DESC";
        List<Entrevista> entrevistas = new ArrayList<>();
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                entrevistas.add(mapRowWithOferta(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando entrevistas", e);
        }
        return entrevistas;
    }

    @Override
    public List<Entrevista> findAll(Connection conn) {
        return List.of();
    }

    @Override
    public Entrevista save(Entrevista entrevista) {
        try (Connection conn = DataSourceFactory.getDataSource().getConnection()) {
            return save(conn, entrevista);
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando entrevista", e);
        }
    }

    @Override
    public Entrevista save(Connection conn, Entrevista entrevista) {
        String sql = "INSERT INTO entrevista (id_oferta, fecha, tipo, notas, resultado) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, getOfertaId(entrevista));
            ps.setDate(2, entrevista.getFecha() != null ? Date.valueOf(entrevista.getFecha()) : null);
            ps.setString(3, entrevista.getTipo());
            ps.setString(4, entrevista.getNotas());
            ps.setString(5, entrevista.getResultado());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entrevista.setId(keys.getInt(1));
                }
            }
            return entrevista;
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando entrevista", e);
        }
    }

    @Override
    public void update(Entrevista entrevista) {
        try (Connection conn = DataSourceFactory.getDataSource().getConnection()) {
            update(conn, entrevista);
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando entrevista con id " + entrevista.getId(), e);
        }
    }

    @Override
    public void update(Connection conn, Entrevista entrevista) {
        String sql = "UPDATE entrevista SET id_oferta = ?, fecha = ?, tipo = ?, notas = ?, resultado = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, getOfertaId(entrevista));
            ps.setDate(2, entrevista.getFecha() != null ? Date.valueOf(entrevista.getFecha()) : null);
            ps.setString(3, entrevista.getTipo());
            ps.setString(4, entrevista.getNotas());
            ps.setString(5, entrevista.getResultado());
            ps.setInt(6, entrevista.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando entrevista con id " + entrevista.getId(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection conn = DataSourceFactory.getDataSource().getConnection()) {
            deleteById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando entrevista con id " + id, e);
        }
    }

    @Override
    public void deleteById(Connection conn, Long id) {
        String sql = "DELETE FROM entrevista WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando entrevista con id " + id, e);
        }
    }

    public List<Entrevista> findByOfertaId(long ofertaId) {
        String sql = BASE_SELECT_WITH_OFERTA + " WHERE en.id_oferta = ? ORDER BY en.fecha DESC, en.id DESC";
        List<Entrevista> entrevistas = new ArrayList<>();
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ofertaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entrevistas.add(mapRowWithOferta(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando entrevistas de la oferta " + ofertaId, e);
        }
        return entrevistas;
    }

    public List<Entrevista> findByResultado(String resultado) {
        String sql = BASE_SELECT_WITH_OFERTA + " WHERE en.resultado = ? ORDER BY en.fecha DESC";
        List<Entrevista> entrevistas = new ArrayList<>();
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resultado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entrevistas.add(mapRowWithOferta(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando entrevistas con resultado " + resultado, e);
        }
        return entrevistas;
    }

    public List<Entrevista> findEntreFechas(LocalDate desde, LocalDate hasta) {
        String sql = BASE_SELECT_WITH_OFERTA + " WHERE en.fecha BETWEEN ? AND ? ORDER BY en.fecha ASC";
        List<Entrevista> entrevistas = new ArrayList<>();
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, desde != null ? Date.valueOf(desde) : Date.valueOf(LocalDate.of(1970, 1, 1)));
            ps.setDate(2, hasta != null ? Date.valueOf(hasta) : Date.valueOf(LocalDate.of(2100, 12, 31)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entrevistas.add(mapRowWithOferta(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando entrevistas entre fechas", e);
        }
        return entrevistas;
    }

    private long getOfertaId(Entrevista entrevista) {
        if (entrevista.getOferta() == null || entrevista.getOferta().getId() <= 0) {
            throw new IllegalArgumentException("La entrevista debe tener una oferta válida con id > 0");
        }
        return entrevista.getOferta().getId();
    }

    private Entrevista mapRowWithOferta(ResultSet rs) throws SQLException {
        Entrevista entrevista = new Entrevista();
        entrevista.setId(rs.getInt("id"));

        Date fechaSql = rs.getDate("fecha");
        entrevista.setFecha(fechaSql != null ? fechaSql.toLocalDate() : null);
        entrevista.setTipo(rs.getString("tipo"));
        entrevista.setNotas(rs.getString("notas"));
        entrevista.setResultado(rs.getString("resultado"));

        long ofertaId = rs.getLong("id_oferta");
        if (ofertaId > 0) {
            Oferta oferta = new Oferta();
            oferta.setId(Math.toIntExact(ofertaId));
            oferta.setTitulo(rs.getString("oferta_titulo"));
            oferta.setDescripcion(rs.getString("oferta_descripcion"));

            String estadoStr = rs.getString("oferta_estado");
            if (estadoStr != null && !estadoStr.isBlank()) {
                oferta.setEstadoCandidatura(EstadoCandidatura.valueOf(estadoStr.trim().toUpperCase()));
            }

            Date fechaPost = rs.getDate("oferta_fecha");
            oferta.setFecha(fechaPost != null ? fechaPost.toLocalDate() : null);
            oferta.setEnlace(rs.getString("oferta_enlace"));
            oferta.setSalario(rs.getDouble("oferta_salario"));
            oferta.setNotas(rs.getString("oferta_notas"));

            Empresa empresa = new Empresa();
            empresa.id = rs.getLong("id_empresa");
            empresa.nombre = rs.getString("empresa_nombre");
            empresa.sector = rs.getString("empresa_sector");
            empresa.web = rs.getString("empresa_web");
            Date fechaReg = rs.getDate("empresa_fecha_registro");
            empresa.fechaRegistro = fechaReg != null ? fechaReg.toLocalDate() : null;

            oferta.setEmpresa(empresa);
            entrevista.setOferta(oferta);
        }
        return entrevista;
    }
}
